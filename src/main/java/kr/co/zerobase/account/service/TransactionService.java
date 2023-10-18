package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.AccountStatus.UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_ALREADY_UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.AMOUNT_EXCEED_BALANCE;
import static kr.co.zerobase.account.type.ErrorCode.CANCEL_MUST_FULLY;
import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.TRANSACTION_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.USER_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.USER_NOT_FOUND;
import static kr.co.zerobase.account.type.TransactionResultType.F;
import static kr.co.zerobase.account.type.TransactionResultType.S;
import static kr.co.zerobase.account.type.TransactionType.CANCEL;
import static kr.co.zerobase.account.type.TransactionType.USE;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.transaction.Transactional;
import kr.co.zerobase.account.domain.Account;
import kr.co.zerobase.account.domain.AccountUser;
import kr.co.zerobase.account.domain.Transaction;
import kr.co.zerobase.account.dto.TransactionDto;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.repository.AccountRepository;
import kr.co.zerobase.account.repository.AccountUserRepository;
import kr.co.zerobase.account.repository.TransactionRepository;
import kr.co.zerobase.account.type.ErrorCode;
import kr.co.zerobase.account.type.TransactionResultType;
import kr.co.zerobase.account.type.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount)
        throws AccountException {
        AccountUser accountUser = getAccountUser(userId);
        Account account = getAccount(accountNumber);

        validateUseBalance(accountUser, account, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveTransaction(USE, S, account, amount, null));
    }

    @Transactional
    public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount) {
        Transaction transaction = getTransaction(transactionId);
        Account account = getAccount(accountNumber);

        validateCancelBalance(transaction, account, amount);

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(saveTransaction(CANCEL, S, account, amount, null));
    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount, ErrorCode errorCode) {
        Account account = getAccount(accountNumber);

        saveTransaction(USE, F, account, amount, errorCode);
    }

    private void validateUseBalance(AccountUser accountUser, Account account, Long amount) {
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }

        if (account.getAccountStatus() == UNREGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }

        if (amount > account.getBalance()) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }
    }

    private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
        if (!Objects.equals(transaction.getAccount().getAccountNumber(),
            account.getAccountNumber())) {
            throw new AccountException(TRANSACTION_ACCOUNT_UN_MATCH);
        }

        if (!Objects.equals(transaction.getAmount(), amount)) {
            throw new AccountException(CANCEL_MUST_FULLY);
        }

        // 과제 조건 명시 X
//        if (transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))) {
//            throw new AccountException(TOO_OLD_ORDER_TO_CANCEL);
//        }
    }

    private Transaction saveTransaction(TransactionType transactionType,
        TransactionResultType transactionResultType,
        Account account,
        Long amount,
        ErrorCode errorCode) {
        return transactionRepository.save(
            Transaction.builder()
                .transactionType(transactionType)
                .transactionResultType(transactionResultType)
                .errorCode(errorCode)
                .account(account)
                .amount(amount)
                .balanceSnapshot(account.getBalance())
                .transactionId(UUID.randomUUID().toString().replace("-", ""))
                .transactedAt(LocalDateTime.now())
                .build());
    }

    private AccountUser getAccountUser(Long userId) {
        return accountUserRepository.findById(userId)
            .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
    }

    private Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));
    }

    private Transaction getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND));
    }
}
