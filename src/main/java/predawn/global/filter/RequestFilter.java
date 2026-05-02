package predawn.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import predawn.global.filter.limiter.RequestLimiter;

import java.io.IOException;

@Slf4j
public class RequestFilter extends OncePerRequestFilter {

    private static final String[] HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private final RequestLimiter requestLimiter;

    public RequestFilter(RequestLimiter requestLimiter) {
        this.requestLimiter = requestLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);

        if (clientIp == null) {
            response.setStatus(400);
            return;
        }

        if (!requestLimiter.allow(clientIp)) {
            response.setStatus(429);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static String getClientIp(HttpServletRequest request) {
        for (String header : HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) return ip.split(",")[0];
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}
