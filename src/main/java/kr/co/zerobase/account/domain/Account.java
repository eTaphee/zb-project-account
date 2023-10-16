package kr.co.zerobase.account.domain;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
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

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private Long balance;

    private LocalDateTime registeredAt;

    private LocalDateTime unregisteredAt;
}
