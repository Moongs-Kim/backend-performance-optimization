package predawn.application.member.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import predawn.domain.common.exception.BadRequestException;
import predawn.domain.member.entity.Friend;
import predawn.domain.member.entity.Member;
import predawn.domain.member.enums.FriendStatus;
import predawn.domain.member.enums.Gender;
import predawn.domain.member.exception.AlreadyRequestFriendException;
import predawn.domain.member.repository.FriendRepository;
import predawn.domain.member.repository.MemberRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class FriendMemberServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FriendRepository friendRepository;

    @DisplayName("회원이 다른 회원에게 친구 요청을 하면 요청 정보가 친구 테이블에 생성된다")
    @Test
    void requestFriend() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        //When
        memberService.requestFriend(fromMember.getId(), toMember.getLoginId());

        em.flush();
        em.clear();

        //Then
        //Friend findFriend = friendRepository.findById(1L).get();
        Friend friend = friendRepository.findByMemberAndFriendMember(fromMember, toMember).get();

        assertThat(friend.getMember().getId()).isEqualTo(fromMember.getId());
        assertThat(friend.getFriendMember().getId()).isEqualTo(toMember.getId());
        assertThat(friend.getMember().getId()).isNotEqualTo(friend.getFriendMember().getId());

        assertThat(friend.getRequestMember().getId()).isEqualTo(fromMember.getId());
        assertThat(friend.getFriendStatus()).isEqualTo(FriendStatus.REQUESTED);
    }

    @DisplayName("회원이 본인 로그인 ID로 친구 요청을 하면 예외가 발생한다")
    @Test
    void requestFriend_FailBySameMember() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);

        memberRepository.save(member);

        //When //Then
        assertThrows(BadRequestException.class, () -> memberService.requestFriend(member.getId(), member.getLoginId()));
    }

    @DisplayName("친구 테이블에 요청중인 상태로 생성이 되어 있으면 예외가 발생한다")
    @Test
    void requestFriend_FailByAlreadyRequestStatus() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        Member findToMember = memberRepository.findMemberByLoginId(toMember.getLoginId());

        memberService.requestFriend(fromMember.getId(), findToMember.getLoginId());

        //When //Then
        assertThrows(AlreadyRequestFriendException.class, () -> memberService.requestFriend(fromMember.getId(), findToMember.getLoginId()));
    }

    @DisplayName("친구 테이블에 친구 상태가 취소일 경우 다시 요청중 상태로 변경된다")
    @Test
    void requestFriend_ChangeFriendStatusByCancel() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        Friend friend = Friend.create(fromMember, toMember, fromMember, FriendStatus.CANCELED);

        friendRepository.save(friend);

        //When
        memberService.requestFriend(fromMember.getId(), toMember.getLoginId());

        em.flush();
        em.clear();

        //Then
        //Friend findFriend = friendRepository.findById(1L).get();
        Friend findFriend = friendRepository.findByMemberAndFriendMember(fromMember, toMember).get();
        assertThat(findFriend.getFriendStatus()).isEqualTo(FriendStatus.REQUESTED);

    }

    @DisplayName("친구 테이블에 친구 상태가 거절일 경우 다시 요청중 상태로 변경된다")
    @Test
    void requestFriend_ChangeFriendStatusByReject() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        Friend friend = Friend.create(fromMember, toMember, fromMember, FriendStatus.REJECTED);

        friendRepository.save(friend);

        //When
        memberService.requestFriend(fromMember.getId(), toMember.getLoginId());

        em.flush();
        em.clear();

        //Then
        //Friend findFriend = friendRepository.findById(1L).get();
        Friend findFriend = friendRepository.findByMemberAndFriendMember(fromMember, toMember).get();
        assertThat(findFriend.getFriendStatus()).isEqualTo(FriendStatus.REQUESTED);

    }

    @DisplayName("친구 테이블에 친구 상태가 승인일 경우 아무런 액션 없이 기존 상태만 반환한다")
    @Test
    void requestFriend_NoActionByAccept() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        Friend friend = Friend.create(fromMember, toMember, fromMember, FriendStatus.ACCEPTED);

        friendRepository.save(friend);

        //When
        FriendStatus friendStatus = memberService.requestFriend(fromMember.getId(), toMember.getLoginId());

        em.flush();
        em.clear();

        //Then
        assertThat(friendStatus).isEqualTo(FriendStatus.ACCEPTED);
    }

    @DisplayName("친구 테이블에 친구 상태가 차단일 경우 아무런 액션 없이 기존 상태만 반환한다")
    @Test
    void requestFriend_NoActionByBlock() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        Friend friend = Friend.create(fromMember, toMember, fromMember, FriendStatus.BLOCKED);

        friendRepository.save(friend);

        //When
        FriendStatus friendStatus = memberService.requestFriend(fromMember.getId(), toMember.getLoginId());

        em.flush();
        em.clear();

        //Then
        assertThat(friendStatus).isEqualTo(FriendStatus.BLOCKED);
    }
}