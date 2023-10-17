package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.AccountStatus.UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_ALREADY_UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.AMOUNT_EXCEED_BALANCE;
import static kr.co.zerobase.account.type.ErrorCode.USER_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.USER_NOT_FOUND;
import static kr.co.zerobase.account.type.TransactionResultType.S;
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

        return TransactionDto.fromEntity(saveTransaction(USE, S, account, amount));
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

    private Transaction saveTransaction(TransactionType transactionType,
        TransactionResultType transactionResultType, Account account, Long amount) {
        return transactionRepository.save(
            Transaction.builder()
                .transactionType(transactionType)
                .transactionResultType(transactionResultType)
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
}
