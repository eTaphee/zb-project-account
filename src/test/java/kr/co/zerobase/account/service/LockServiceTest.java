package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_LOCK;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import kr.co.zerobase.account.exception.AccountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @InjectMocks
    private LockService lockService;

    @Test
    @DisplayName("락 획득 성공")
    void successLock() throws InterruptedException {
        // given
        given(redissonClient.getLock(anyString()))
            .willReturn(lock);

        given(lock.tryLock(anyLong(), anyLong(), any()))
            .willReturn(true);

        // when
        // then
        assertDoesNotThrow(() ->
            lockService.lock("lock-key"));
    }

    @Test
    @DisplayName("락 획득 실패")
    void failLock() throws InterruptedException {
        // given
        given(redissonClient.getLock(anyString()))
            .willReturn(lock);

        given(lock.tryLock(anyLong(), anyLong(), any()))
            .willReturn(false);

        // when
        AccountException exception = assertThrows(AccountException.class, () ->
            lockService.lock("lock-key"));

        // then
        assertEquals(TRANSACTION_LOCK, exception.getErrorCode());
    }
}