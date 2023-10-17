package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.AccountStatus.IN_USE;
import static kr.co.zerobase.account.type.AccountStatus.UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_ALREADY_UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static kr.co.zerobase.account.type.ErrorCode.ACCOUNT_NUMBER_ALREADY_EXISTS;
import static kr.co.zerobase.account.type.ErrorCode.BALANCE_NOT_EMPTY;
import static kr.co.zerobase.account.type.ErrorCode.USER_ACCOUNT_UN_MATCH;
import static kr.co.zerobase.account.type.ErrorCode.USER_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import kr.co.zerobase.account.domain.Account;
import kr.co.zerobase.account.domain.AccountUser;
import kr.co.zerobase.account.dto.AccountDto;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.repository.AccountRepository;
import kr.co.zerobase.account.repository.AccountSpecification;
import kr.co.zerobase.account.repository.AccountUserRepository;
import kr.co.zerobase.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private static final int ACCOUNT_NUMBER_LENGTH = 10;
    private static final int MAX_ACCOUNT_PER_USER = 10;
    private static final Random random = new Random();

    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        AccountUser accountUser = getAccountUser(userId);

        validateCreateAccount(accountUser);

        try {
            return AccountDto.fromEntity(
                accountRepository.saveAndFlush(Account.builder()
                    .accountUser(accountUser)
                    .accountNumber(generateAccountNumber())
                    .accountStatus(IN_USE)
                    .balance(initialBalance)
                    .registeredAt(LocalDateTime.now())
                    .build()));
        } catch (DataIntegrityViolationException e) {
            // TODO: lock aop로 around시 중복키에 대한 예외가 발생할 수 있을까?
            throw new AccountException(ACCOUNT_NUMBER_ALREADY_EXISTS);
        }
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);
        Account account = getAccount(accountNumber);

        validateDeleteAccount(accountUser, account);

        account.unregister();
        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }

    @Transactional
    public List<AccountDto> getAccountsByUserId(long userId) {
        AccountUser accountUser = getAccountUser(userId);

        List<Account> accounts = accountRepository.findAll(
            getAccountSpecByAccountUserAndAccountStatusEqualsInUse(accountUser));

        return accounts.stream().map(AccountDto::fromEntity)
            .collect(Collectors.toList());
    }

    private static Specification<Account> getAccountSpecByAccountUserAndAccountStatusEqualsInUse(
        AccountUser accountUser) {
        // TODO: 해지 계좌 포함 여부
        return AccountSpecification.equalAccountUser(accountUser)
            .and(AccountSpecification.equalAccountStatus(IN_USE));
    }

    private String generateAccountNumber() {
        StringBuilder buffer = new StringBuilder();
        Optional<Account> account = null;

        do {
            buffer.delete(0, buffer.length());
            for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
                buffer.append(random.nextInt(10));
            }
            account = accountRepository.findByAccountNumber(buffer.toString());
        } while (account.isPresent());

        return buffer.toString();
    }

    private void validateCreateAccount(AccountUser accountUser) {
        // TODO: 해지 계좌도 카운팅에 포함 여부
        if (accountRepository.count(
            getAccountSpecByAccountUserAndAccountStatusEqualsInUse(accountUser))
            >= MAX_ACCOUNT_PER_USER) {
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER);
        }
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }

        if (account.getAccountStatus() == UNREGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }

        if (account.getBalance() > 0) {
            throw new AccountException(BALANCE_NOT_EMPTY);
        }
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
