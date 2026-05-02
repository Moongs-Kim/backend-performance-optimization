package predawn.infrastructure.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MemberRedisRepositoryTest {

    @Autowired
    private MemberRedisRepository memberRedisRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void cleanUp() {
        redisTemplate
            .getConnectionFactory()
            .getConnection()
            .serverCommands()
            .flushAll();
    }

    @DisplayName("인증 토큰을 통해 회원 ID를 저장할 수 있다")
    @Test
    void savePasswordResetToken() {
        //Given
        String memberId = "1";
        String token = "token";

        //When
        memberRedisRepository.savePasswordResetToken(token, memberId);

        //Then
        String findMemberId = memberRedisRepository.findMemberIdBy(token).get();

        assertThat(findMemberId).isEqualTo(memberId);
    }

    @DisplayName("인증 토큰을 통해 회원 ID를 조회할 수 있다")
    @Test
    void findMemberIdBy() {
        //Given
        String memberId = "1";
        String token = "token";

        memberRedisRepository.savePasswordResetToken(token, memberId);

        //When
        String findMemberId = memberRedisRepository.findMemberIdBy(token).get();

        //Then
        assertThat(findMemberId).isEqualTo(memberId);
    }

    @DisplayName("등록되지 않은 인증 토큰으로 회원 ID를 조회할 수 없다")
    @Test
    void findMemberIdBy_NotFound() {
        //Given
        String memberId = "1";
        String token = "token";
        String anotherToken = "another-token";

        memberRedisRepository.savePasswordResetToken(token, memberId);

        //When
        Optional<String> possibleMemberId = memberRedisRepository.findMemberIdBy(anotherToken);

        assertThat(possibleMemberId.isEmpty()).isTrue();

    }

    @DisplayName("토큰으로 저장한 key는 해당 토큰으로 삭제할 수 있다")
    @Test
    void deletePasswordResetKey() {
        //Given
        String memberId = "1";
        String token = "token";

        memberRedisRepository.savePasswordResetToken(token, memberId);

        //When
        memberRedisRepository.deletePasswordResetKey(token);

        //Then
        Optional<String> possibleMemberId = memberRedisRepository.findMemberIdBy(token);

        assertThat(possibleMemberId.isEmpty()).isTrue();
    }

    @DisplayName("토큰으로 저장한 key는 해당 토큰이 아니면 삭제할 수 없다")
    @Test
    void deletePasswordResetKey_Fail() {
        //Given
        String memberId = "1";
        String token = "token";
        String anotherToken = "another-token";

        memberRedisRepository.savePasswordResetToken(token, memberId);

        //When
        memberRedisRepository.deletePasswordResetKey(anotherToken);

        //Then
        Optional<String> possibleMemberId = memberRedisRepository.findMemberIdBy(token);

        assertThat(possibleMemberId.isEmpty()).isFalse();
        assertThat(possibleMemberId.get()).isEqualTo(memberId);
    }
}