package predawn.global.filter.limiter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import predawn.infrastructure.redis.RequestLimiterRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestLimiter {
    private static final long REQUEST_LIMIT_TIME_MS = 5_000;
    private static final int MAX_REQUESTS = 15;

    private final RequestLimiterRepository requestLimiterRepository;

    public boolean allow(String clientIp) {
        long now = System.currentTimeMillis();

        String requestId = UUID.randomUUID().toString();

        requestLimiterRepository.removeZSetRangeFromZero(clientIp, now - REQUEST_LIMIT_TIME_MS);

        requestLimiterRepository.addZSet(clientIp, requestId, now);

        requestLimiterRepository.setZSetExpire(clientIp, REQUEST_LIMIT_TIME_MS);

        Long count = requestLimiterRepository.getZSetCount(clientIp);

        return count <= MAX_REQUESTS;
    }

}
