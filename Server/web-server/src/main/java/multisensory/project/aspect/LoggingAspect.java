package multisensory.project.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* multisensory.project.service.*.*(..))")
    public void serviceMethods() {}

    @Before("serviceMethods()")
    public void logBeforeService(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        logger.info("{} Method is about to execute", methodName);
    }

    @AfterReturning(value = "serviceMethods()")
    public void logAfterService(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        logger.info("{} Method executed successfully", methodName);
    }

    @AfterThrowing(pointcut = "execution(* multisensory.project.controller.*.*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
        logger.error("Exception occurred in controller method: {}", joinPoint.getSignature().getName());
        logger.error("Exception message: {}", ex.getMessage());
    }
}