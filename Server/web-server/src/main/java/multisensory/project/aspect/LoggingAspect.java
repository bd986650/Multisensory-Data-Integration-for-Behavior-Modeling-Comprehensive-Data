package multisensory.project.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Указываем, что аспект будет работать для всех методов в сервисах
    @Pointcut("execution(* multisensory.project.service.*.*(..))")
    public void serviceMethods() {}

    // Логирование до выполнения метода
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        logger.info("{} Method is about to execute", methodName);
    }

    // Логирование после успешного выполнения метода
    @AfterReturning(value = "serviceMethods()")
    public void logAfterReturning(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        logger.info("{} Method executed successfully", methodName);
    }
} 

