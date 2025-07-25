package capstone.bookitty.global.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Around("execution(* capstone.bookitty.domain.*.application.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();
        
        // Log business method start
        log.info("[Service] {}.{} 비즈니스 로직 시작 - 파라미터: {}", 
            className, methodName, Arrays.toString(args));
        
        try {
            // Execute the service method
            Object result = joinPoint.proceed();
            
            // Log successful business logic completion
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[Service] {}.{} 비즈니스 로직 성공 완료 - 실행시간: {}ms", 
                className, methodName, executionTime);
            
            return result;
            
        } catch (Exception e) {
            // Log business logic exception
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[Service] {}.{} 비즈니스 로직 실행 중 예외 발생 - 실행시간: {}ms, 예외: {}", 
                className, methodName, executionTime, e.getMessage(), e);
            throw e;
        }
    }
}