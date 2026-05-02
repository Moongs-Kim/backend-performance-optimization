package predawn.global.filter.limiter;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import predawn.infrastructure.redis.RequestLimiterRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class RequestLimiterTest {

    private static final int MAX_REQUESTS = 30;

    @Autowired
    private RequestLimiter requestLimiter;

    @Autowired
    private RequestLimiterRepository requestLimiterRepository;

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

    @DisplayName("지정된 횟수보다 요청이 같거나 낮으면 추가로 더 요청을 할 수 있다")
    @Test
    void allow() {
        //Given
        String clientIp = "192.168.0.1";

        addZSetsLessThenOneLimit(clientIp);

        //When
        boolean allow = requestLimiter.allow(clientIp);

        //Then
        assertThat(allow).isTrue();
    }

    @DisplayName("지정된 횟수보다 요청이 많으면 더 이상 요청할 수 없다")
    @Test
    void allow_Fail() {
        //Given
        String clientIp = "192.168.0.1";

        addZSetsEqualsToLimit(clientIp);

        //When
        boolean allow = requestLimiter.allow(clientIp);

        //Then
        assertThat(allow).isFalse();
    }

    private void addZSetsLessThenOneLimit(String clientIp) {
        for (int i = 1; i <= MAX_REQUESTS - 1; i++) {
            String requestId = "requestId" + i;
            requestLimiter.allow(clientIp);
        }
    }

    private void addZSetsEqualsToLimit(String clientIp) {
        for (int i = 1; i <= MAX_REQUESTS; i++) {
            String requestId = "requestId" + i;
            requestLimiter.allow(clientIp);
        }
    }

}