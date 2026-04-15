package predawn.application.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import predawn.application.file.enums.RootDirectory;
import predawn.application.file.mapper.FileMapper;
import predawn.application.member.dto.MemberSignupCommand;
import predawn.application.member.factory.MemberFactory;
import predawn.domain.file.entity.UploadFile;
import predawn.domain.file.repository.FileStorage;
import predawn.domain.file.repository.UploadFileRepository;
import predawn.domain.file.vo.StoredFile;
import predawn.domain.member.entity.Member;
import predawn.domain.member.repository.MemberRepository;
import predawn.infrastructure.file.local.LocalFileStorage;
import predawn.web.member.session.LoginMember;

import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final UploadFileRepository uploadFileRepository;
    private final FileStorage fileStorage;

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
}
