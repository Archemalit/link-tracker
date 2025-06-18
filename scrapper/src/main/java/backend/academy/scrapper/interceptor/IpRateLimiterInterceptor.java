package backend.academy.scrapper.interceptor;

import backend.academy.scrapper.exception.NotFoundIpException;
import backend.academy.scrapper.exception.TooManyRequests;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limiter.scrapper.enabled", havingValue = "true", matchIfMissing = true)
public class IpRateLimiterInterceptor implements HandlerInterceptor {
    private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private final RateLimiterConfig rateLimiterConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For")).orElse(request.getRemoteAddr());
        if (Objects.isNull(ip)) {
            throw new NotFoundIpException("Ваш IP адрес подозрителен!");
        }
        if (!rateLimiters.computeIfAbsent(ip, this::createRateLimiter).acquirePermission()) {
            throw new TooManyRequests(ip);
        }
        return true;
    }

    private RateLimiter createRateLimiter(String ip) {
        return RateLimiter.of(ip, rateLimiterConfig);
    }
}
