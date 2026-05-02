package predawn.global.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisConnectionValidator implements ApplicationListener<ApplicationReadyEvent> {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            redisConnectionFactory.getConnection().ping();
        } catch (Exception e) {
            log.error("Redis 서버에 연결할 수 없습니다.");
            System.exit(1);
        }
    }
}
