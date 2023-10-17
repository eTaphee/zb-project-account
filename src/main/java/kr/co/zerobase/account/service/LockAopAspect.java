package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.ErrorCode.CREATE_ACCOUNT_TRANSACTION_LOCK;
import static kr.co.zerobase.account.type.ErrorCode.MODIFY_ACCOUNT_TRANSACTION_LOCK;
import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_LOCK;

import kr.co.zerobase.account.aop.ModifyAccountRequest;
import kr.co.zerobase.account.exception.AccountException;
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
            if (e.getErrorCode() == TRANSACTION_LOCK) {
                throw new AccountException(CREATE_ACCOUNT_TRANSACTION_LOCK);
            }
            throw e;
        }

        try {
            return pjp.proceed();
        } finally {
            lockService.unlock("CreateAccountLock");
        }
    }

    @Around("@annotation(kr.co.zerobase.account.aop.ModifyAccountLock) && args(request)")
    public Object aroundMethod(
        ProceedingJoinPoint pjp
        , ModifyAccountRequest request) throws Throwable {

        String lockKey = getLockKey(request);

        try {
            lockService.lock(lockKey);
        } catch (AccountException e) {
            if (e.getErrorCode() == TRANSACTION_LOCK) {
                throw new AccountException(MODIFY_ACCOUNT_TRANSACTION_LOCK);
            }
            throw e;
        }

        try {
            return pjp.proceed();
        } finally {
            lockService.unlock(lockKey);
        }
    }

    private static String getLockKey(ModifyAccountRequest request) {
        return "ModifyAccountLock:" + request.getAccountNumber();
    }
}
