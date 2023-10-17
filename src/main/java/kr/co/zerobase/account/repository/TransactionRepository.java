package kr.co.zerobase.account.repository;

import kr.co.zerobase.account.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
