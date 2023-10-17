package kr.co.zerobase.account.dto;

import static kr.co.zerobase.account.type.ValidationMessage.INITIAL_BALANCE_MIN_0;
import static kr.co.zerobase.account.type.ValidationMessage.INITIAL_BALANCE_NOT_NULL;
import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_MIN_1;
import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_NOT_NULL;

import java.time.LocalDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class CreateAccount {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RequestDto {
        @NotNull(message = USER_ID_NOT_NULL)
        @Min(value = 1, message = USER_ID_MIN_1)
        private final Long userId;

        @NotNull(message = INITIAL_BALANCE_NOT_NULL)
        @Min(value = 0, message = INITIAL_BALANCE_MIN_0)
        private final Long initialBalance;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ResponseDto {
        private final Long userId;
        private final String accountNumber;
        private final LocalDateTime registeredAt;

        public static ResponseDto from(AccountDto accountDto) {
            return ResponseDto.builder()
                .userId(accountDto.getUserId())
                .accountNumber(accountDto.getAccountNumber())
                .registeredAt(accountDto.getRegisteredAt())
                .build();
        }
    }
}
