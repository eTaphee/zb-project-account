package kr.co.zerobase.account.dto;

import java.time.LocalDateTime;
import kr.co.zerobase.account.type.TransactionResultType;
import kr.co.zerobase.account.type.TransactionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class GetTransaction {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ResponseDto {

        private final String accountNumber;
        private final TransactionType transactionType;
        private final TransactionResultType transactionResult;
        private final String transactionId;
        private final Long amount;
        private final LocalDateTime transactedAt;

        public static ResponseDto from(TransactionDto transactionDto) {
            return ResponseDto.builder()
                .accountNumber(transactionDto.getAccountNumber())
                .transactionType(transactionDto.getTransactionType())
                .transactionResult(transactionDto.getTransactionResultType())
                .transactionId(transactionDto.getTransactionId())
                .amount(transactionDto.getAmount())
                .transactedAt(transactionDto.getTransactedAt())
                .build();
        }
    }
}
