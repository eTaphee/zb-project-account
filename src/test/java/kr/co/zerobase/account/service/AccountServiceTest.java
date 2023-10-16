package kr.co.zerobase.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kr.co.zerobase.account.domain.Account;
import kr.co.zerobase.account.domain.AccountUser;
import kr.co.zerobase.account.dto.AccountDto;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.repository.AccountRepository;
import kr.co.zerobase.account.repository.AccountUserRepository;
import kr.co.zerobase.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void successCreateAccount() {
        AccountUser user = AccountUser.builder()
            .id(1L)
            .name("Pobi")
            .build();

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(user));

        given(accountRepository.save(any()))
            .willReturn(Account.builder()
                .accountUser(user)
                .accountNumber("1000000000")
                .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(1L, accountDto.getUserId());
        assertEquals("1000000000", accountDto.getAccountNumber());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 사용자 없음")
    void failCreateAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
            () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 사용자당 생성 개수 초과")
    void failCreateAccount_MaxAccountPerUser() {
        // given
        AccountUser user = AccountUser.builder()
            .id(1L)
            .name("Pobi")
            .build();

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(user));

        given(accountRepository.countByAccountUser(any()))
            .willReturn(10);

        // when
        AccountException accountException = assertThrows(AccountException.class,
            () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER, accountException.getErrorCode());
    }
}