package com.example.desafio.service;

import com.example.desafio.model.Account;
import com.example.desafio.model.Transaction;
import com.example.desafio.model.User; 
import com.example.desafio.repository.AccountRepository;
import com.example.desafio.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account createAccount(Account account) {
        User userLogado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if ("CUSTOMER".equals(userLogado.getRole())) {
            account.setBalance(BigDecimal.ZERO);
            account.setUserId(userLogado.getId());
        } else {
            if (account.getBalance() == null) {
                account.setBalance(BigDecimal.ZERO);
            }
        }
        
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

        User userLogado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if ("CUSTOMER".equals(userLogado.getRole())) {
            if (!account.getUserId().equals(userLogado.getId())) {
                throw new SecurityException("Não é possível consultar os dados de outra conta");
            }
        }

        return account;
    }

    @Transactional
    public void closeAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

        User userLogado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (!"ADMIN".equals(userLogado.getRole())) {
            if (!account.getUserId().equals(userLogado.getId())) {
                throw new SecurityException("Não é permitido encerrar conta de terceiros");
            }
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Não é possível encerrar uma conta com saldo diferente de zero");
        }
        
        accountRepository.delete(account);
    }
    
    @Transactional(readOnly = true)
    public List<Transaction> getAccountStatement(Long accountId, int limit) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Conta informada não existe"));

        User userLogado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if ("CUSTOMER".equals(userLogado.getRole())) {
            if (!account.getUserId().equals(userLogado.getId())) {
                throw new SecurityException("Não é possível consultar os dados de outra conta");
            }
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findLatestTransactions(accountId, pageable);
    }
}