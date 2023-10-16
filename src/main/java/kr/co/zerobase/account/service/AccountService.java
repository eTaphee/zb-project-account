package kr.co.zerobase.account.service;

import static kr.co.zerobase.account.type.AccountStatus.IN_USE;
import static kr.co.zerobase.account.type.ErrorCode.USER_NOT_FOUND;

import java.time.LocalDateTime;
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
import org.springframework.stereotype.Service;

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

        return AccountDto.fromEntity(
            accountRepository.save(Account.builder()
                .accountUser(accountUser)
                .accountNumber(generateAccountNumber())
                .accountStatus(IN_USE)
                .balance(initialBalance)
                .registeredAt(LocalDateTime.now())
                .build()));
    }

    private void validateAccountUser(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= MAX_ACCOUNT_PER_USER) {
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER);
        }
    }

    private static String generateAccountNumber() {
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            buffer.append(random.nextInt(10));
        }

        return buffer.toString();
    }
}
