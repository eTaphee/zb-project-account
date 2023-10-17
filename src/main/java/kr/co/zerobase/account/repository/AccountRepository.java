package kr.co.zerobase.account.repository;

import java.util.List;
import java.util.Optional;
import kr.co.zerobase.account.domain.Account;
import kr.co.zerobase.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>,
    JpaSpecificationExecutor<Account> {

//    Integer countByAccountUser(AccountUser accountUser);

    Optional<Account> findByAccountNumber(String accountNumber);

//    List<Account> findAllByAccountUser(AccountUser accountUser);
}
