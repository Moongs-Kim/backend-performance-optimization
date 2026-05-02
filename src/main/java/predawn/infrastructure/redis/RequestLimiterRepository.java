package predawn.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RequestLimiterRepository {

    private static final String REQUEST_LIMIT_KEY_PREFIX = "request_limit:";

    private final RedisTemplate<String, String> redisTemplate;

    public void removeZSetRangeFromZero(String keySuffix, double max) {
        redisTemplate
            .opsForZSet()
            .removeRangeByScore(REQUEST_LIMIT_KEY_PREFIX + keySuffix, 0, max);
    }

    public void addZSet(String keySuffix, String value, double score) {
        redisTemplate
            .opsForZSet()
            .add(REQUEST_LIMIT_KEY_PREFIX + keySuffix, value, score);
    }

    public Long getZSetCount(String keySuffix) {
        return redisTemplate
            .opsForZSet()
            .size(REQUEST_LIMIT_KEY_PREFIX + keySuffix);
    }

    public void setZSetExpire(String keySuffix, long timeMs) {
        redisTemplate.expire(
                REQUEST_LIMIT_KEY_PREFIX + keySuffix,
                timeMs,
                TimeUnit.MILLISECONDS
        );
    }
}
