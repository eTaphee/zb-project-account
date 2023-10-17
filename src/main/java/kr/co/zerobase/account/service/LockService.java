package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_LOCK;

import java.util.concurrent.TimeUnit;
import kr.co.zerobase.account.exception.AccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {

    private final RedissonClient redissonClient;

    public void lock(String key) throws InterruptedException {
        RLock lock = redissonClient.getLock(key);
        log.debug("Trying lock for {}", key);

        try {
            boolean isLock = lock.tryLock(2, 15, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("=========Lock acq failed=========");
                throw new AccountException(TRANSACTION_LOCK);
            }
        } catch (AccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis lock failed", e);
            throw e;
        }
    }

    public void unlock(String key) {
        log.debug("Unlock for {}", key);
        redissonClient.getLock(key).unlock();
    }
}
