package kr.co.zerobase.account.dto;

import static kr.co.zerobase.account.type.ValidationMessage.ACCOUNT_NUMBER_NOT_NULL;
import static kr.co.zerobase.account.type.ValidationMessage.ACCOUNT_NUMBER_SIZE_10;
import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_MIN_1;
import static kr.co.zerobase.account.type.ValidationMessage.USER_ID_NOT_NULL;
import static kr.co.zerobase.account.type.ValidationMessage.USE_BALANCE_AMOUNT_MAX_1_000_000_000;
import static kr.co.zerobase.account.type.ValidationMessage.USE_BALANCE_AMOUNT_MIN_10;
import static kr.co.zerobase.account.type.ValidationMessage.USE_BALANCE_AMOUNT_NOT_NULL;

import java.time.LocalDateTime;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.co.zerobase.account.aop.ModifyAccountRequest;
import kr.co.zerobase.account.type.TransactionResultType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UseBalance {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RequestDto implements ModifyAccountRequest {

        @NotNull(message = USER_ID_NOT_NULL)
        @Min(value = 1, message = USER_ID_MIN_1)
        private final Long userId;

        @NotNull(message = ACCOUNT_NUMBER_NOT_NULL)
        @Size(min = 10, max = 10, message = ACCOUNT_NUMBER_SIZE_10)
        private final String accountNumber;

        @NotNull(message = USE_BALANCE_AMOUNT_NOT_NULL)
        @Min(value = 10, message = USE_BALANCE_AMOUNT_MIN_10)
        @Max(value = 1_000_000_000, message = USE_BALANCE_AMOUNT_MAX_1_000_000_000)
        private final Long amount;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ResponseDto {

        private final String accountNumber;
        private final TransactionResultType transactionResult;
        private final String transactionId;
        private final Long amount;
        private final LocalDateTime transactedAt;

        public static ResponseDto from(TransactionDto transactionDto) {
            return ResponseDto.builder()
                .accountNumber(transactionDto.getAccountNumber())
                .transactionResult(transactionDto.getTransactionResultType())
                .transactionId(transactionDto.getTransactionId())
                .amount(transactionDto.getAmount())
                .transactedAt(transactionDto.getTransactedAt())
                .build();
        }
    }
}
