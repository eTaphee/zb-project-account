package kr.co.zerobase.account.service;

import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {

    private final LockService lockService;

    @Around("@annotation(kr.co.zerobase.account.aop.CreateAccountLock)")
    public Object aroundMethod(
        ProceedingJoinPoint pjp) throws Throwable {
        try {
            lockService.lock("CreateAccountLock");
        } catch (AccountException e) {
            if (e.getErrorCode() == ErrorCode.TRANSACTION_LOCK) {
                throw new AccountException(ErrorCode.CREATE_ACCOUNT_TRANSACTION_LOCK);
            }
            throw e;
        }

        try {
            return pjp.proceed();
        } finally {
            lockService.unlock("CreateAccountLock");
        }
    }
}
