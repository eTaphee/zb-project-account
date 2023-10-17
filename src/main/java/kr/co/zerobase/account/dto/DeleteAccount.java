package kr.co.zerobase.account.dto;

import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_MIN_1;
import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_NOT_NULL;

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
    public static class RequestDto {

        @NotNull(message = USER_ID_NOT_NULL)
        @Min(value = 1, message = USER_ID_MIN_1)
        private final Long userId;

        @JsonCreator
        private RequestDto(@JsonProperty("userId") Long userId) {
            this.userId = userId;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ResponseDto {

        private final Long userId;
        private final String accountNumber;
        private final LocalDateTime unregisteredAt;

        public static ResponseDto from(AccountDto accountDto) {
            return ResponseDto.builder()
                .userId(accountDto.getUserId())
                .accountNumber(accountDto.getAccountNumber())
                .unregisteredAt(accountDto.getUnregisteredAt())
                .build();
        }
    }
}
