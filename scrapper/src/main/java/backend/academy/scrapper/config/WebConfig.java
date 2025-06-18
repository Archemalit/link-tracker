package backend.academy.scrapper.config;

import backend.academy.scrapper.interceptor.IpRateLimiterInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final ObjectProvider<IpRateLimiterInterceptor> ipRateLimiterInterceptorProvider;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        ipRateLimiterInterceptorProvider.ifAvailable(registry::addInterceptor);
    }
}
