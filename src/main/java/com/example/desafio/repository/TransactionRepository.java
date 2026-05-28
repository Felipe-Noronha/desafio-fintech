package com.example.desafio.repository;

import com.example.desafio.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.payerAccountId = :accountId OR t.payeeAccountId = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findLatestTransactions(@Param("accountId") Long accountId, Pageable pageable);
}