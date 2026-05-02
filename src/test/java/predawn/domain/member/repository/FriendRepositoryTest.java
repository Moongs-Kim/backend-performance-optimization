package predawn.domain.member.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import predawn.domain.member.entity.Friend;
import predawn.domain.member.entity.Member;
import predawn.domain.member.enums.FriendStatus;
import predawn.domain.member.enums.Gender;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class FriendRepositoryTest {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    @DisplayName("친구 테이블에 회원, 친구 회원을 저장할 수 있다")
    @Test
    void save() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        Friend friend = Friend.create(fromMember, toMember, fromMember, FriendStatus.REQUESTED);

        //When
        Friend savedFriend = friendRepository.save(friend);

        //Then
        assertThat(savedFriend).isEqualTo(savedFriend);
    }

    @DisplayName("친구 테이블에 회원, 친구 회원을 저장할때 회원 ID가 더 적은 회원을 앞쪽 파라미터에 두고 저장해야 한다")
    @Test
    void save_FailByLessThanMemberIdNotFirstCheckConstraint() {
        //Given
        Member fristMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member secondmember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fristMember);
        memberRepository.save(secondmember);

        Friend friend = Friend.create(secondmember, fristMember, fristMember, FriendStatus.REQUESTED);

        //When //Then
        assertThrows(DataIntegrityViolationException.class, () -> friendRepository.save(friend));
    }

    @DisplayName("친구 테이블에 이미 저장된 회원, 친구 회원을 저장하면 유니크 제악 조건 예외가 발생한다")
    @Test
    void save_FailByUniqueConstraint() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        Friend friend = Friend.create(fromMember, toMember, fromMember, FriendStatus.REQUESTED);
        Friend notUniqueFriend = Friend.create(fromMember, toMember, fromMember, FriendStatus.REQUESTED);

        friendRepository.save(friend);

        //When //Then
        assertThrows(DataIntegrityViolationException.class, () -> friendRepository.save(notUniqueFriend));
    }

    @DisplayName("회원 ID와 상대 회원 ID로 친구 테이블의 데이터를 찾을 수 있다")
    @Test
    void findByMemberIdAndFriendMemberId() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        Friend friend = Friend.create(fromMember, toMember, fromMember, FriendStatus.REQUESTED);

        friendRepository.save(friend);

        //When
        Friend findFriend = friendRepository.findByMemberAndFriendMember(fromMember, toMember).get();

        //Then
        assertThat(findFriend.getMember().getId()).isEqualTo(fromMember.getId());
        assertThat(findFriend.getFriendMember().getId()).isEqualTo(toMember.getId());
        assertThat(findFriend.getFriendStatus()).isEqualTo(FriendStatus.REQUESTED);
    }

    @DisplayName("친구 테이블에 데이터가 있다면 true를 반환한다")
    @Test
    void existsByMemberIdAndFriendMemberId() {
        //Given
        Member fromMember = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user1@test.com", "주소", null);
        Member toMember = new Member("user2", "1234", "user2", LocalDate.of(2000, 1, 1),
                Gender.MALE, "user2@test.com", "주소", null);

        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        Friend friend = Friend.create(fromMember, toMember, fromMember, FriendStatus.REQUESTED);

        friendRepository.save(friend);

        //When
        boolean isExists = friendRepository.existsByMemberAndFriendMember(fromMember, toMember);

        //Then
        assertThat(isExists).isTrue();
    }

}