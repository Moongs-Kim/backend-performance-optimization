package predawn.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberRedisRepository {

    private static final String PASSWORD_RESET_KEY_PREFIX = "pw:reset:";

    private final RedisTemplate<String, String> redisTemplate;

    public Optional<String> findMemberIdBy(String passwordResetToken) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PASSWORD_RESET_KEY_PREFIX + passwordResetToken));
    }

    public void savePasswordResetToken(String passwordResetToken, String memberId) {
        redisTemplate.opsForValue()
                .set(
                    PASSWORD_RESET_KEY_PREFIX + passwordResetToken,
                    memberId,
                    Duration.ofMinutes(5)
                );
    }

    public boolean deletePasswordResetKey(String passwordResetToken) {
        return redisTemplate.delete(PASSWORD_RESET_KEY_PREFIX + passwordResetToken);
    }
}
