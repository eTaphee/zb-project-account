package kr.co.zerobase.account.dto;

import java.time.LocalDateTime;
import kr.co.zerobase.account.domain.Account;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountDto {
    private final Long userId;
    private final String accountNumber;
    private final Long balance;
    private final LocalDateTime registeredAt;
    private final LocalDateTime unregisteredAt;

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
            .userId(account.getAccountUser().getId())
            .accountNumber(account.getAccountNumber())
            .balance(account.getBalance())
            .registeredAt(account.getRegisteredAt())
            .unregisteredAt(account.getUnregisteredAt())
            .build();
    }
}
