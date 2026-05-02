package predawn.infrastructure.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RequestLimiterRepositoryTest {

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

    @DisplayName("Redis ZSet에 값을 저장할 수 있다")
    @Test
    void addZSet() {
        //Given
        String clientIp = "192.168.0.1";
        String requestId = "requestId";
        long timeMs = 10_000;

        //When
        requestLimiterRepository.addZSet(clientIp, requestId, timeMs);

        //Then
        Long count = requestLimiterRepository.getZSetCount(clientIp);
        assertThat(count).isOne();

        Set<String> zSetValues = redisTemplate.opsForZSet().range("request_limit:" + clientIp, 0, -1);
        String zSetValue = zSetValues.stream()
                .findFirst()
                .get();

        assertThat(zSetValue).isEqualTo(requestId);
    }

    @DisplayName("0 ~ 지정된 시간(ms) 까지의 모든 값은 삭제된다")
    @Test
    void removeZSetRangeFromZero() {
        //Given
        String clientIp = "192.168.0.1";
        long timeMs = 11_000;

        addZSets(clientIp);

        //When
        requestLimiterRepository.removeZSetRangeFromZero(clientIp, timeMs);

        //Then
        Long count = requestLimiterRepository.getZSetCount(clientIp);
        assertThat(count).isZero();
    }

    @DisplayName("지정된 시간(ms) 이후의 값은 삭제되지 않고 남아있다")
    @Test
    void removeZSetRangeFromZero_RemainValue() {
        //Given
        String clientIp = "192.168.0.1";
        long timeMs = 9_000;

        addZSets(clientIp);

        //When
        requestLimiterRepository.removeZSetRangeFromZero(clientIp, timeMs);

        //Then
        Long count = requestLimiterRepository.getZSetCount(clientIp);
        assertThat(count).isEqualTo(3);
    }

    @DisplayName("저장한 값의 ttl을 설정할 수 있다")
    @Test
    void setZSetExpire() {
        //Given
        String clientIp = "192.168.0.1";
        String requestId = "requestId";
        long timeMs = 10_000;
        long expireTimeMs = 5_000;

        requestLimiterRepository.addZSet(clientIp, requestId, timeMs);

        //When
        requestLimiterRepository.setZSetExpire(clientIp, expireTimeMs);

        //Then
        Long expire = redisTemplate.getExpire("request_limit:" + clientIp);

        assertThat(expire).isNotNull();
        assertThat(expire).isLessThanOrEqualTo(expireTimeMs);
    }

    private void addZSets(String clientIp) {
        long timeMs = 10_000;
        for (int i = 1; i <= 3; i++) {
            String requestId = "requestId" + i;
            timeMs = timeMs + i;
            requestLimiterRepository.addZSet(clientIp, requestId, timeMs);
        }
    }


}