package capstone.bookitty.global.aspect;

import capstone.bookitty.global.notification.CriticalErrorNotificationService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ControllerLoggingAspect {

    private final CriticalErrorNotificationService criticalErrorNotificationService;

    @Around("execution(* capstone.bookitty.domain.*.api.*.*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();
        
        // Log method start with HTTP context
        log.info("[Controller] {}.{} 요청 시작 - 파라미터: {}", 
            className, methodName, Arrays.toString(args));
        
        try {
            // Execute the controller method
            Object result = joinPoint.proceed();
            
            // Log successful response
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[Controller] {}.{} 요청 성공 완료 - 응답시간: {}ms", 
                className, methodName, executionTime);
            
            return result;
            
        } catch (Exception e) {
            // Log controller exception
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[Controller] {}.{} 요청 처리 중 예외 발생 - 응답시간: {}ms, 예외: {}", 
                className, methodName, executionTime, e.getMessage(), e);
            
            // Handle critical error notification
            criticalErrorNotificationService.handleException(e, "Controller", className, methodName, executionTime);
            
            throw e;
        }
    }
}