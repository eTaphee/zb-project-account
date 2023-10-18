package kr.co.zerobase.account.domain;

import static kr.co.zerobase.account.type.AccountStatus.UNREGISTERED;
import static kr.co.zerobase.account.type.ErrorCode.AMOUNT_EXCEED_BALANCE;
import static kr.co.zerobase.account.type.ErrorCode.INVALID_REQUEST;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.type.AccountStatus;
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
public class Account extends BaseEntity {

    @ManyToOne
    private AccountUser accountUser;

    @Column(unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private Long balance;

    private LocalDateTime registeredAt;

    private LocalDateTime unregisteredAt;

    public void unregister() {
        accountStatus = UNREGISTERED;
        unregisteredAt = LocalDateTime.now();
    }

    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }

        balance -= amount;
    }

    public void cancelBalance(Long amount) {
        if (amount < 0) {
            throw new AccountException(INVALID_REQUEST);
        }

        balance += amount;
    }
}
