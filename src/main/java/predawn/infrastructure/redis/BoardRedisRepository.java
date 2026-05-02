package predawn.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BoardRedisRepository {

    private static final String BOARD_VIEW_KEY_PREFIX = "view:board:";

    private final RedisTemplate<String, String> redisTemplate;

    public Boolean saveViewIfNotExists(Long boardId, Long memberId) {
        return redisTemplate.opsForValue()
                .setIfAbsent(
                        BOARD_VIEW_KEY_PREFIX + boardId + "member:" + memberId,
                        "1",
                        Duration.ofHours(1)
                );
    }

}
