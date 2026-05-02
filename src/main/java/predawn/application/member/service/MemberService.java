package predawn.application.member.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import predawn.application.file.enums.RootDirectory;
import predawn.application.file.mapper.FileMapper;
import predawn.application.member.dto.MemberSignupCommand;
import predawn.application.member.dto.PasswordChangeCommand;
import predawn.application.member.factory.MemberFactory;
import predawn.domain.file.entity.UploadFile;
import predawn.domain.file.repository.FileStorage;
import predawn.domain.file.repository.UploadFileRepository;
import predawn.domain.file.vo.StoredFile;
import predawn.domain.member.entity.Friend;
import predawn.domain.member.entity.Member;
import predawn.domain.member.enums.FriendStatus;
import predawn.domain.member.exception.AlreadyRequestFriendException;
import predawn.domain.member.exception.AuthenticationFailException;
import predawn.domain.member.exception.MemberNotFoundException;
import predawn.domain.member.repository.FriendRepository;
import predawn.domain.member.repository.MemberRepository;
import predawn.global.error.exception.BusinessException;
import predawn.infrastructure.file.local.LocalFileStorage;
import predawn.infrastructure.redis.MemberRedisRepository;
import predawn.web.member.session.LoginMember;
import predawn.domain.common.exception.BadRequestException;

import java.util.*;
import java.util.stream.Stream;


@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final UploadFileRepository uploadFileRepository;
    private final FileStorage fileStorage;
    private final MemberRedisRepository memberRedisRepository;
    private final FriendRepository friendRepository;
    private final EntityManager em;

    @Transactional
    public void signup(MemberSignupCommand signupCommand, MultipartFile profileImage) {
        StoredFile storedFile = fileStorage.store(RootDirectory.MEMBER, profileImage);

        UploadFile uploadFile =
                storedFile != null
                        ? uploadFileRepository.save(FileMapper.toEntity(storedFile))
                        : null;

        memberRepository.save(MemberFactory.create(signupCommand, uploadFile));
    }

    @Transactional
    public LoginMember login(String loginId, String password) {
        Optional<Member> possibleMember = memberRepository.login(loginId, password);

        if (possibleMember.isEmpty()) return null;
        Member member = possibleMember.get();

        String filePath = null;
        if (member.getProfileImage() != null) {
            UploadFile proFileImage = member.getProfileImage();
            filePath = (fileStorage instanceof LocalFileStorage localFileStorage)
                    ? localFileStorage.getViewPath(proFileImage.getStorageKey())
                    : fileStorage.getFullPath(proFileImage.getStorageKey());
        }

        return new LoginMember(member.getId(), member.getName(), filePath);
    }

    public String findMemberLoginId(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        return member.getLoginId();
    }

    public String createPasswordResetToken(String loginId, String email) {
        Member member = memberRepository.findByLoginIdAndEmail(loginId, email).orElseThrow(MemberNotFoundException::new);

        String token = UUID.randomUUID().toString();

        memberRedisRepository.savePasswordResetToken(token, String.valueOf(member.getId()));

        return token;
    }

    @Transactional
    public void passwordChange(PasswordChangeCommand passwordChangeCommand) {
        String token = passwordChangeCommand.getToken();
        String memberId = memberRedisRepository.findMemberIdBy(token)
                .orElseThrow(AuthenticationFailException::new);

        Member member = memberRepository.findById(Long.parseLong(memberId)).orElseThrow(MemberNotFoundException::new);

        member.changePassword(passwordChangeCommand.getPassword());

        memberRedisRepository.deletePasswordResetKey(token);
    }

    @Transactional
    public FriendStatus requestFriend(Long memberId, String loginId) {
        Member requestMember = em.getReference(Member.class, memberId);
        Member friendMember = memberRepository.findMemberByLoginId(loginId);

        isMemberExistsFor(friendMember);

        mustNotSame(requestMember, friendMember);

        List<Member> ascMembers = sortMemberByMemberIdToAsc(requestMember, friendMember);

        Optional<Friend> possibleFriend = friendRepository.findByMemberAndFriendMember(ascMembers.getFirst(), ascMembers.get(1));

        return (possibleFriend.isEmpty())
                ? getFriendStatusAfterSaveFriend(Friend.create(ascMembers.getFirst(), ascMembers.get(1), requestMember, FriendStatus.REQUESTED))
                : getFriendStatusAfterCheckAndAction(possibleFriend.get());
    }

    private FriendStatus getFriendStatusAfterCheckAndAction(Friend friend) {
        switch (friend.getFriendStatus()) {
            case REQUESTED -> throwAlreadyRequestFriendException();
            case REJECTED, CANCELED -> changeFriendStatusToRequest(friend);
        }
        return friend.getFriendStatus();
    }

    private void changeFriendStatusToRequest(Friend friend) {
        friend.changeFriendStatus(FriendStatus.REQUESTED);
    }

    private void isMemberExistsFor(Member friendMember) {
        if (friendMember == null) throw new MemberNotFoundException();
    }

    private List<Member> sortMemberByMemberIdToAsc(Member requestMember, Member friendMember) {
        return Stream.of(requestMember, friendMember)
                .sorted(Comparator.comparingLong(Member::getId))
                .toList();
    }

    private void mustNotSame(Member requestMember, Member friendMember) {
        if (Objects.equals(requestMember.getId(), friendMember.getId())) {
            throw new BadRequestException();
        }
    }

    private FriendStatus getFriendStatusAfterSaveFriend(Friend friend) {
        try {
            Friend savedFriend = friendRepository.save(friend);
            return savedFriend.getFriendStatus();
        } catch (DataIntegrityViolationException e) {
            Throwable cause = e.getCause();

            if (cause instanceof ConstraintViolationException ex) {
                String constraintName = ex.getConstraintName();

                if ("uq_member_id_friend_member_id".equalsIgnoreCase(constraintName) ||
                        "PUBLIC.UQ_MEMBER_ID_FRIEND_MEMBER_ID_INDEX_7".equalsIgnoreCase(constraintName))
                {
                    throwAlreadyRequestFriendException();
                }
            }
            throw new BusinessException(e);
        }
    }

    private void throwAlreadyRequestFriendException() {
        throw new AlreadyRequestFriendException();
    }
}
