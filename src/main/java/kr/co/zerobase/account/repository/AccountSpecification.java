package kr.co.zerobase.account.repository;

import kr.co.zerobase.account.domain.Account;
import kr.co.zerobase.account.domain.AccountUser;
import kr.co.zerobase.account.type.AccountStatus;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecification {

    public static Specification<Account> equalAccountUser(AccountUser accountUser) {
        return (root, query, criteriaBuilder)
            -> criteriaBuilder.equal(root.get("accountUser"), accountUser);
    }

    public static Specification<Account> equalAccountStatus(AccountStatus accountStatus) {
        return (root, query, criteriaBuilder)
            -> criteriaBuilder.equal(root.get("accountStatus"), accountStatus);
    }
}
