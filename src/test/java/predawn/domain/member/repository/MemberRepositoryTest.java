package predawn.domain.member.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import predawn.domain.member.entity.Member;
import predawn.domain.member.enums.Gender;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("이메일로 회원 한명을 조회할 수 있다")
    @Test
    void findByEmail() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When
        Member findMember = memberRepository.findByEmail(member.getEmail()).get();

        //Then
        assertThat(findMember.getLoginId()).isEqualTo("user1");
        assertThat(findMember.getEmail()).isEqualTo("test@test.com");
    }

    @DisplayName("존재하지 않는 이메일로 회원을 조회할 수 없다")
    @Test
    void findByEmail_NotFound() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When
        Optional<Member> possibleMember = memberRepository.findByEmail("email@notExists.com");

        //Then
        assertThat(possibleMember.isEmpty()).isTrue();
        assertThrows(NoSuchElementException.class, () -> possibleMember.get());
    }

    @DisplayName("로그인 ID와 이메일로 회원을 조회할 수 있다")
    @Test
    void findByLoginIdAndEmail() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When
        Member findMember = memberRepository.findByLoginIdAndEmail("user1", "test@test.com").get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getLoginId()).isEqualTo("user1");
        assertThat(findMember.getEmail()).isEqualTo("test@test.com");
    }

    @DisplayName("이메일은 일치해도 로그인 ID가 일치하지 않으면 회원을 조회할 수 없다")
    @Test
    void findByLoginIdAndEmail_NotExistsLoginId() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When
        Optional<Member> possibleMember = memberRepository.findByLoginIdAndEmail("not-exists-id", "test@test.com");

        //Then
        assertThat(possibleMember.isEmpty()).isTrue();
        assertThrows(NoSuchElementException.class, () -> possibleMember.get());
    }

    @DisplayName("로그인 ID는 일치해도 이메일이 일치하지 않으면 회원을 조회할 수 없다")
    @Test
    void findByLoginIdAndEmail_NotExistsEmail() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When
        Optional<Member> possibleMember = memberRepository.findByLoginIdAndEmail("user1", "email@notExists.com");

        //Then
        assertThat(possibleMember.isEmpty()).isTrue();
        assertThrows(NoSuchElementException.class, () -> possibleMember.get());
    }

}