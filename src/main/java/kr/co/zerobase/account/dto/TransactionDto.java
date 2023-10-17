package kr.co.zerobase.account.dto;

import java.time.LocalDateTime;
import kr.co.zerobase.account.domain.Transaction;
import kr.co.zerobase.account.type.TransactionResultType;
import kr.co.zerobase.account.type.TransactionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionDto {

    private final String accountNumber;
    private final TransactionType transactionType;
    private final TransactionResultType transactionResultType;
    private final Long amount;
    private final Long balanceSnapshot;
    private final String transactionId;
    private final LocalDateTime transactedAt;

    public static TransactionDto fromEntity(Transaction transaction) {
        return TransactionDto.builder()
            .accountNumber(transaction.getAccount().getAccountNumber())
            .transactionType(transaction.getTransactionType())
            .transactionResultType(transaction.getTransactionResultType())
            .amount(transaction.getAmount())
            .balanceSnapshot(transaction.getBalanceSnapshot())
            .transactionId(transaction.getTransactionId().toString())
            .transactedAt(transaction.getTransactedAt())
            .build();
    }
}
