package backend.academy.scrapper.metric;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class CountUserMessageAspect {
    private final UserMessageMetric userMessageMetric;

    @Pointcut("@annotation(CountUserMessage)")
    public void countUserMessageMethods() {}

    @Before("countUserMessageMethods()")
    public void before() {
        userMessageMetric.increment();
    }
}
