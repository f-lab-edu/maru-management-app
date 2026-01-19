package com.maru.common.aop;

import com.maru.security.DojangAccessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 도장 접근 검증 AOP
 *
 * @see AopOrder
 */
@Slf4j
@Aspect
@Component
@Order(AopOrder.SECURITY_VALIDATION)
@RequiredArgsConstructor
public class DojangAccessAspect {

    private final DojangAccessValidator validator;

    // 1. 클래스에 @ValidateDojangAccess가 붙어있는지 확인
    @Pointcut("@within(validateDojangAccess)")
    public void hasValidateDojangAccess(ValidateDojangAccess validateDojangAccess) {}

    // 2. public 메서드인지 확인
    @Pointcut("execution(public * *(..))")
    public void isPublicMethod() {}

    // 3. @SkipDojangValidation이 붙어있지 않은지 확인
    @Pointcut("!@annotation(com.maru.common.aop.SkipDojangValidation)")
    public void isNotSkipped() {}

    @Before("hasValidateDojangAccess(validateDojangAccess) && isPublicMethod() && isNotSkipped()")
    public void validateAccess(JoinPoint joinPoint, ValidateDojangAccess validateDojangAccess) {
        String paramName = validateDojangAccess.paramName();
        String dojangId = extractDojangId(joinPoint, paramName);

        if (dojangId == null) {
            log.debug("dojangId 파라미터 없음, 검증 스킵: {}", joinPoint.getSignature().toShortString());
            return;
        }

        validator.validate(dojangId);
    }

    private String extractDojangId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = signature.getParameterNames();

        if (parameterNames == null) {
            return null;
        }

        for (int i = 0; i < parameterNames.length; i++) {
            if (paramName.equals(parameterNames[i])) {
                return (String) args[i];
            }
        }
        return null;
    }
}
