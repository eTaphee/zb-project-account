package kr.co.zerobase.account.domain;

import static kr.co.zerobase.account.type.ErrorCode.CANCEL_BALANCE_MUST_SUCCESS_TRANSACTION;
import static kr.co.zerobase.account.type.ErrorCode.CANCEL_BALANCE_MUST_USE_TRANSACTION;
import static kr.co.zerobase.account.type.TransactionResultType.F;
import static kr.co.zerobase.account.type.TransactionType.CANCEL;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.type.ErrorCode;
import kr.co.zerobase.account.type.TransactionResultType;
import kr.co.zerobase.account.type.TransactionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Transaction extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;

    @Enumerated(EnumType.STRING)
    private ErrorCode errorCode;

    @ManyToOne
    private Account account;

    private long amount;

    private long balanceSnapshot;

    @Column(unique = true)
    private String transactionId;

    private LocalDateTime transactedAt;

    @OneToOne
    private Transaction transactionForCancel;

    private boolean isCanceled;

    public void cancel() {
        // TODO service에서 유효성 검사 하는데 entity method에서도 유효성 검사가 필요한가?
        if (transactionType == CANCEL) {
            throw new AccountException(CANCEL_BALANCE_MUST_USE_TRANSACTION);
        }

        if (transactionResultType == F) {
            throw new AccountException(CANCEL_BALANCE_MUST_SUCCESS_TRANSACTION);
        }

        isCanceled = true;
    }
}
