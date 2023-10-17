package kr.co.zerobase.account.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class DeleteAccount {

    @Getter
    @Builder
    public static class Request {
        @NotNull(message = "사용자 아이디는 빈 값일 수 없습니다.")
        @Min(value = 1, message = "사용자 아이디는 1 이상이어야 합니다.")
        private final Long userId;

        @JsonCreator
        private Request(@JsonProperty("userId") Long userId) {
            this.userId = userId;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response {

        private final Long userId;
        private final String accountNumber;
        private final LocalDateTime unregisteredAt;

        public static DeleteAccount.Response from(AccountDto accountDto) {
            return Response.builder()
                .userId(accountDto.getUserId())
                .accountNumber(accountDto.getAccountNumber())
                .unregisteredAt(accountDto.getUnregisteredAt())
                .build();
        }
    }
}
