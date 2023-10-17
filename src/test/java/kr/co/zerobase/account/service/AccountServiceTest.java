package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.AccountStatus.UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_ALREADY_UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.BALANCE_NOT_EMPTY;
import static kr.co.zerobase.account.type.ErrorCode.MAX_ACCOUNT_PER_USER;
import static kr.co.zerobase.account.type.ErrorCode.USER_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import kr.co.zerobase.account.domain.Account;
import kr.co.zerobase.account.domain.AccountUser;
import kr.co.zerobase.account.dto.AccountDto;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.repository.AccountRepository;
import kr.co.zerobase.account.repository.AccountUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    private final AccountUser pobi = AccountUser.builder()
        .id(1L)
        .name("Pobi")
        .build();

    private final AccountUser harry = AccountUser.builder()
        .id(2L)
        .name("Harry")
        .build();

    @Test
    @DisplayName("계좌 생성 성공")
    void successCreateAccount() {
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.empty());

        given(accountRepository.saveAndFlush(any()))
            .willReturn(Account.builder()
                .accountUser(pobi)
                .accountNumber("1000000000")
                .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        // then
        verify(accountRepository, times(1)).saveAndFlush(captor.capture());
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
        assertEquals(USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 사용자당 생성 개수 초과")
    void failCreateAccount_MaxAccountPerUser() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.count(any(Specification.class)))
            .willReturn(10L);

        // when
        AccountException accountException = assertThrows(AccountException.class,
            () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(MAX_ACCOUNT_PER_USER, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 성공")
    void successDeleteAccount() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(Account.builder()
                .accountUser(pobi)
                .balance(0L)
                .accountNumber("1000000000")
                .build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.deleteAccount(1L, "1000000000");

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(1L, accountDto.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
        assertEquals(UNREGISTERED, captor.getValue().getAccountStatus());
        assertNotNull(captor.getValue().getUnregisteredAt());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 사용자 없음")
    void failDeleteAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.deleteAccount(1L, "1000000000"));

        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 계좌 없음")
    void failDeleteAccount_AccountNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.deleteAccount(1L, "1000000000"));

        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 계좌 소유주 다름")
    void failDeleteAccount_UserAccountUnMatch() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(Account.builder()
                .accountUser(harry)
                .balance(0L)
                .accountNumber("1000000000")
                .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.deleteAccount(1L, "1000000000"));

        // then
        assertEquals(USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 잔액 있음")
    void failDeleteAccount_BalanceNotEmpty() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(Account.builder()
                .accountUser(pobi)
                .balance(10L)
                .accountNumber("1000000000")
                .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.deleteAccount(1L, "1000000000"));

        // then
        assertEquals(BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 이미 해지됨")
    void failDeleteAccount_AccountAlreadyUnregistered() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(Account.builder()
                .accountUser(pobi)
                .balance(0L)
                .accountStatus(UNREGISTERED)
                .accountNumber("1000000000")
                .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.deleteAccount(1L, "1000000000"));

        // then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 확인 성공")
    void successGetAccountsByUserId() {
        // given
        List<Account> accounts = Arrays.asList(
            Account.builder().accountUser(pobi).accountNumber("1000000000").balance(0L).build(),
            Account.builder().accountUser(pobi).accountNumber("2000000000").balance(100L).build(),
            Account.builder().accountUser(pobi).accountNumber("3000000000").balance(2000L).build()
        );

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findAll(any(Specification.class)))
            .willReturn(accounts);

        // when
        List<AccountDto> accountDtos = accountService.getAccountsByUserId(1L);

        // then
        assertEquals(3, accountDtos.size());
        assertEquals("1000000000", accountDtos.get(0).getAccountNumber());
        assertEquals(0L, accountDtos.get(0).getBalance());
        assertEquals("2000000000", accountDtos.get(1).getAccountNumber());
        assertEquals(100L, accountDtos.get(1).getBalance());
        assertEquals("3000000000", accountDtos.get(2).getAccountNumber());
        assertEquals(2000L, accountDtos.get(2).getBalance());
    }

    @Test
    @DisplayName("계좌 확인 실패 - 사용자 없음")
    void failGetAccountsByUserId_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
            () -> accountService.getAccountsByUserId(1L));

        // then
        assertEquals(USER_NOT_FOUND, accountException.getErrorCode());
    }
}