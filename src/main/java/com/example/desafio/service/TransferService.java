package com.example.desafio.service;

import com.example.desafio.model.Account;
import com.example.desafio.model.Transaction;
import com.example.desafio.model.User;
import com.example.desafio.repository.AccountRepository;
import com.example.desafio.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    private final Map<String, Boolean> processedRequests = new ConcurrentHashMap<>();

    public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000); 
        factory.setReadTimeout(3000);   
        
        this.restTemplate = new RestTemplate(factory);
    }

    @Transactional
    public void executeTransfer(Long payerId, Long payeeId, BigDecimal amount) {
        if (payerId.equals(payeeId)) {
            throw new IllegalArgumentException("Não é possível transferir valores para a mesma conta");
        }

        String requestKey = payerId + "_" + payeeId + "_" + amount;
        if (processedRequests.containsKey(requestKey)) {
            throw new IllegalStateException("Esta transação já está sendo processada");
        }
        processedRequests.put(requestKey, true);

        try {
            Long firstLockId = Math.min(payerId, payeeId);
            Long secondLockId = Math.max(payerId, payeeId);

            Account firstAccount = accountRepository.findByIdWithLock(firstLockId)
                    .orElseThrow(() -> new IllegalArgumentException("Conta de ID " + firstLockId + " não encontrada"));
            
            Account secondAccount = accountRepository.findByIdWithLock(secondLockId)
                    .orElseThrow(() -> new IllegalArgumentException("Conta de ID " + secondLockId + " não encontrada"));

            Account payerAccount = firstAccount.getId().equals(payerId) ? firstAccount : secondAccount;
            Account payeeAccount = firstAccount.getId().equals(payeeId) ? firstAccount : secondAccount;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User userLogado = (User) authentication.getPrincipal();

                if ("CUSTOMER".equals(userLogado.getRole())) {
                    if (!payerAccount.getUserId().equals(userLogado.getId())) {
                        throw new SecurityException("A conta de origem informada não pertence ao seu usuário");
                    }
                }
            }

            if (payerAccount.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Saldo insuficiente para realizar a transferência");
            }

            UUID transactionId = UUID.randomUUID();
            boolean isAuthorized = authorizeTransaction(transactionId, payerId, payeeId, amount);
            if (!isAuthorized) {
                throw new SecurityException("Transferência recusada pelo serviço autorizador externo");
            }

            payerAccount.setBalance(payerAccount.getBalance().subtract(amount));
            payeeAccount.setBalance(payeeAccount.getBalance().add(amount));

            accountRepository.save(payerAccount);
            accountRepository.save(payeeAccount);

            Transaction transaction = new Transaction(
                    transactionId,
                    payerAccount.getId(),
                    payeeAccount.getId(),
                    amount,
                    LocalDateTime.now(),
                    "SUCCESS"
            );
            transactionRepository.save(transaction);
            
        } finally {
            processedRequests.remove(requestKey);
        }
    }

    private boolean authorizeTransaction(UUID transactionId, Long payerId, Long payeeId, BigDecimal amount) {
        try {
            String url = "https://desafio.rivs.com.br/api/v1/authorize";

            Map<String, Object> requestBody = new ConcurrentHashMap<>();
            requestBody.put("transactionId", transactionId.toString());
            requestBody.put("payerId", payerId);
            requestBody.put("payeeId", payeeId);
            requestBody.put("amount", amount);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean authorized = (Boolean) response.getBody().get("authorized");
                return authorized != null && authorized;
            }
            return false;
        } catch (Exception e) {
            return false; 
        }
    }
}