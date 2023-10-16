package kr.co.zerobase.account.dto;

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
    public static class Request {
        @NotNull(message = "사용자 아이디는 빈 값일 수 없습니다.")
        @Min(value = 1, message = "사용자 아이디는 1 이상이어야 합니다.")
        private final Long userId;

        @NotNull(message = "초기 잔액은 빈 값일 수 없습니다.")
        @Min(value = 0, message = "초기 잔액은 0 이상이어야 합니다.")
        private Long initialBalance;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response {
        private final Long userId;
        private final String accountNumber;
        private final LocalDateTime registeredAt;

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                .userId(accountDto.getUserId())
                .accountNumber(accountDto.getAccountNumber())
                .registeredAt(accountDto.getRegisteredAt())
                .build();
        }
    }
}
