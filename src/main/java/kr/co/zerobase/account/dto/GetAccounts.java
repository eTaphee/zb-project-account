package kr.co.zerobase.account.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class GetAccounts {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AccountInfoDto {

        private final String accountNumber;
        private final long balance;

        public static AccountInfoDto from(AccountDto accountDto) {
            return AccountInfoDto.builder()
                .accountNumber(accountDto.getAccountNumber())
                .balance(accountDto.getBalance())
                .build();
        }
    }
}
