package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.AccountStatus.IN_USE;
import static kr.co.zerobase.account.type.ErrorCode.ALREADY_EXIST_ACCOUNT_NUMBER;
import static kr.co.zerobase.account.type.ErrorCode.USER_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import javax.transaction.Transactional;
import kr.co.zerobase.account.domain.Account;
import kr.co.zerobase.account.domain.AccountUser;
import kr.co.zerobase.account.dto.AccountDto;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.repository.AccountRepository;
import kr.co.zerobase.account.repository.AccountUserRepository;
import kr.co.zerobase.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
        AccountUser accountUser = accountUserRepository.findById(userId)
            .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        validateAccountUser(accountUser);

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
            throw new AccountException(ALREADY_EXIST_ACCOUNT_NUMBER);
        }
    }

    private void validateAccountUser(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= MAX_ACCOUNT_PER_USER) {
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER);
        }
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
}
