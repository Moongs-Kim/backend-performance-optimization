package predawn.application.member.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import predawn.application.member.dto.PasswordChangeCommand;
import predawn.domain.member.entity.Member;
import predawn.domain.member.enums.Gender;
import predawn.domain.member.exception.AuthenticationFailException;
import predawn.domain.member.exception.MemberNotFoundException;
import predawn.domain.member.repository.MemberRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DisplayName("이메일로 회원의 로그인 ID를 조회할 수 있다")
    @Test
    void findMemberLoginId() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When
        String memberLoginId = memberService.findMemberLoginId(member.getEmail());

        //Then
        assertThat(memberLoginId).isEqualTo("user1");
    }

    @DisplayName("존재하지 않는 이메일로 회원 로그인 ID를 조회하면 예외가 발생한다")
    @Test
    void findMemberLoginId_NotFound() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When //Then
        assertThrows(MemberNotFoundException.class, () -> memberService.findMemberLoginId("email@notExists.com"));
    }

    @DisplayName("로그인 ID와 이메일로 회원을 조회하면 토큰을 만들어 준다")
    @Test
    void createPasswordResetToken() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When
        String token = memberService.createPasswordResetToken("user1", "test@test.com");

        //Then
        assertThat(token).isNotBlank();
    }

    @DisplayName("존재하지 않는 로그인 ID로 회원을 조회하면 예외가 발생한다")
    @Test
    void createPasswordResetToken_NotExistsLoginId() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When //Then

        assertThrows(MemberNotFoundException.class, () -> memberService.createPasswordResetToken("not-exists-id", "test@test.com"));
    }

    @DisplayName("존재하지 않는 이메일로 회원을 조회하면 예외가 발생한다")
    @Test
    void createPasswordResetToken_NotExistsEmail() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        //When //Then

        assertThrows(MemberNotFoundException.class, () -> memberService.createPasswordResetToken("user1", "email@notExists.com"));
    }

    @DisplayName("토큰을 통해 인증하면 비밀번호를 변경할 수 있다")
    @Test
    void passwordChange() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        String token = memberService.createPasswordResetToken("user1", "test@test.com");

        PasswordChangeCommand passwordChangeCommand = PasswordChangeCommand.of(token, "4321");

        //When
        memberService.passwordChange(passwordChangeCommand);

        em.flush();
        em.clear();

        //Then
        Member passwordChangedMember = memberRepository.findById(member.getId()).get();

        assertThat(passwordChangedMember.getPassword()).isEqualTo("4321");
    }

    @DisplayName("비밀번호 변경 후 인증 정보는 Redis에서 삭제된다")
    @Test
    void passwordChange_after_token_delete() {
        //Given
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "test@test.com", "주소", null);

        memberRepository.save(member);

        String token = memberService.createPasswordResetToken("user1", "test@test.com");

        PasswordChangeCommand passwordChangeCommand = PasswordChangeCommand.of(token, "4321");

        memberService.passwordChange(passwordChangeCommand);

        em.flush();
        em.clear();

        //When
        String emptyValue = redisTemplate.opsForValue().get("pw:reset:" + token);

        //Then
        assertThat(emptyValue).isNull();
    }

    @DisplayName("토큰을 통해 인증 정보를 조회할 수 없으면 예외가 발생한다")
    @Test
    void passwordChange_authentication_fail() {
        //Given
        PasswordChangeCommand passwordChangeCommand = PasswordChangeCommand.of("wrong-token", "new-password");

        //When //Then
        assertThrows(AuthenticationFailException.class, () -> memberService.passwordChange(passwordChangeCommand));
    }
}