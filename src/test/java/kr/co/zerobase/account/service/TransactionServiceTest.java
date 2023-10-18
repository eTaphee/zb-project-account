package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.AccountStatus.IN_USE;
import static kr.co.zerobase.account.type.AccountStatus.UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_ALREADY_UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.AMOUNT_EXCEED_BALANCE;
import static kr.co.zerobase.account.type.ErrorCode.CANCEL_BALANCE_MUST_SUCCESS_TRANSACTION;
import static kr.co.zerobase.account.type.ErrorCode.CANCEL_BALANCE_MUST_USE_TRANSACTION;
import static kr.co.zerobase.account.type.ErrorCode.CANCEL_MUST_FULLY;
import static kr.co.zerobase.account.type.ErrorCode.INVALID_REQUEST;
import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_ALREADY_CANCELED;
import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.USER_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.USER_NOT_FOUND;
import static kr.co.zerobase.account.type.TransactionResultType.F;
import static kr.co.zerobase.account.type.TransactionResultType.S;
import static kr.co.zerobase.account.type.TransactionType.CANCEL;
import static kr.co.zerobase.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.co.zerobase.account.domain.Account;
import kr.co.zerobase.account.domain.AccountUser;
import kr.co.zerobase.account.domain.Transaction;
import kr.co.zerobase.account.dto.TransactionDto;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.repository.AccountRepository;
import kr.co.zerobase.account.repository.AccountUserRepository;
import kr.co.zerobase.account.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private final AccountUser pobi = AccountUser.builder()
        .id(1L)
        .name("Pobi")
        .build();

    private final AccountUser harry = AccountUser.builder()
        .id(2L)
        .name("Harry")
        .build();

    public static final long CANCEL_AMOUNT = 200L;

    @Test
    @DisplayName("잔액 사용 성공")
    void successUseBalance() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        Account account = Account.builder()
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(10000L)
            .accountNumber("1000000000")
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
            .willReturn(Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto transactionDto = transactionService.useBalance(1L,
            "1000000000", 1000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(9000L, captor.getValue().getBalanceSnapshot());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 사용자 없음")
    void failUseBalance_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "10000000", 1000L));

        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 계좌 없음")
    void failUseBalance_AccountNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1234567890", 1000L));

        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 계좌 소유주 다름")
    void failUseBalance_UserAccountUnMatch() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(Account.builder()
                .accountUser(harry)
                .balance(0L)
                .accountNumber("1000000012")
                .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1000000012", 1000L));

        // then
        assertEquals(USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 이미 해지된 계좌")
    void failUseBalance_AccountAlreadyUnregistered() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(Account.builder()
                .accountUser(pobi)
                .balance(0L)
                .accountStatus(UNREGISTERED)
                .accountNumber("1000000012")
                .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1000000012", 1000L));

        // then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 거래 금액이 잔액보다 큼")
    void failUseBalance_AmountExceedBalance() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(pobi));

        Account account = Account.builder()
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(100L)
            .accountNumber("1000000012")
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1000000012", 1000L));

        // then
        assertEquals(AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("실패 트랜잭션 저장 성공")
    void successSaveFailedUseTransaction() {
        // given
        Account account = Account.builder()
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(10000L)
            .accountNumber("1000000012")
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        transactionService.saveFailedUseTransaction(USE, "100000000",
            2000L, INVALID_REQUEST);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(2000L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(F, captor.getValue().getTransactionResultType());
        assertEquals(INVALID_REQUEST, captor.getValue().getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 성공")
    void successCancelBalance() {
        // given
        Account account = Account.builder()
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(10000L)
            .accountNumber("1000000012")
            .build();

        Transaction transaction = Transaction.builder()
            .account(account)
            .transactionType(USE)
            .transactionResultType(S)
            .transactionId("transactionId")
            .transactedAt(LocalDateTime.now())
            .amount(CANCEL_AMOUNT)
            .balanceSnapshot(9000L)
            .build();

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
            .willReturn(Transaction.builder()
                .account(account)
                .transactionType(CANCEL)
                .transactionResultType(S)
                .transactionId("canceledTransactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(10000L)
                .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto transactionDto = transactionService.cancelBalance("transactionId",
            String.valueOf(1000000000), CANCEL_AMOUNT);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
        assertEquals(10000L + CANCEL_AMOUNT, captor.getValue().getBalanceSnapshot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(10000L, transactionDto.getBalanceSnapshot());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
    }

    @Test
    @DisplayName("거래 취소 실패 - 계좌 없음")
    void failCancelBalance_AccountNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(Transaction.builder().build()));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionId", "1234567890", 1000L));

        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 - 거래 없음")
    void failCancelBalance_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionId", "1234567890", 1000L));

        // then
        assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 - 계좌 불일치")
    void failCancelBalance_TransactionAccountUnMatch() {
        // given
        Account account = Account.builder()
            .id(1L)
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(1000L)
            .accountNumber("1000000000")
            .build();

        Account accountNotUse = Account.builder()
            .id(2L)
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(1000L)
            .accountNumber("1000000001")
            .build();

        Transaction transaction = Transaction.builder()
            .account(account)
            .transactionType(USE)
            .transactionResultType(S)
            .transactionId("transactionId")
            .transactedAt(LocalDateTime.now())
            .amount(CANCEL_AMOUNT)
            .balanceSnapshot(9000L)
            .build();

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(accountNotUse));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionId", "1000000012", CANCEL_AMOUNT));

        // then
        assertEquals(TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 - 부분 취소 불가")
    void failCancelBalance_CancelMustFully() {
        // given
        Account account = Account.builder()
            .id(1L)
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(1000L)
            .accountNumber("1000000000")
            .build();

        Transaction transaction = Transaction.builder()
            .account(account)
            .transactionType(USE)
            .transactionResultType(S)
            .transactionId("transactionId")
            .transactedAt(LocalDateTime.now())
            .amount(CANCEL_AMOUNT)
            .balanceSnapshot(9000L)
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(transaction));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionId", "1000000012", 300L));

        // then
        assertEquals(CANCEL_MUST_FULLY, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 - 이미 취소된 거래")
    void failCancelBalance_TransactionAlreadyCanceled() {
        // given
        Account account = Account.builder()
            .id(1L)
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(1000L)
            .accountNumber("1000000000")
            .build();

        Transaction transaction = Transaction.builder()
            .account(account)
            .isCanceled(true)
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(transaction));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionId", "1000000012", 300L));

        // then
        assertEquals(TRANSACTION_ALREADY_CANCELED, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 - 취소된 거래 취소")
    void failCancelBalance_CancelBalanceMustUseTransaction() {
        // given
        Account account = Account.builder()
            .id(1L)
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(1000L)
            .accountNumber("1000000000")
            .build();

        Transaction transaction = Transaction.builder()
            .account(account)
            .transactionType(CANCEL)
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(transaction));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionId", "1000000012", 300L));

        // then
        assertEquals(CANCEL_BALANCE_MUST_USE_TRANSACTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 - 실패한 거래 취소")
    void failCancelBalance_CancelBalanceMustSuccessTransaction() {
        // given
        Account account = Account.builder()
            .id(1L)
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(1000L)
            .accountNumber("1000000000")
            .build();

        Transaction transaction = Transaction.builder()
            .account(account)
            .transactionResultType(F)
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(transaction));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionId", "1000000012", 300L));

        // then
        assertEquals(CANCEL_BALANCE_MUST_SUCCESS_TRANSACTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 조회 성공")
    void successQueryTransaction() {
        // given
        Account account = Account.builder()
            .id(1L)
            .accountUser(pobi)
            .accountStatus(IN_USE)
            .balance(1000L)
            .accountNumber("1000000000")
            .build();

        Transaction transaction = Transaction.builder()
            .account(account)
            .transactionType(USE)
            .transactionResultType(S)
            .transactionId("transactionId")
            .transactedAt(LocalDateTime.now().minusYears(1).minusSeconds(1))
            .amount(CANCEL_AMOUNT)
            .balanceSnapshot(9000L)
            .build();

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(transaction));

        // when
        TransactionDto transactionDto = transactionService.queryTransaction("trxId");

        // then
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
        assertEquals("transactionId", transactionDto.getTransactionId());
    }

    @Test
    @DisplayName("거래 조회 실패 - 거래 없음")
    void failQueryTransaction_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.queryTransaction("transactionId"));

        // then
        assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
}