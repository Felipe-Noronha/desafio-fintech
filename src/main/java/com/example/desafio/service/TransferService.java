package com.example.desafio.service;

import com.example.desafio.model.Account;
import com.example.desafio.model.Transaction;
import com.example.desafio.model.User;
import com.example.desafio.repository.AccountRepository;
import com.example.desafio.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
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
    private final ObjectMapper objectMapper;

    private final Map<String, Boolean> processedRequests = new ConcurrentHashMap<>();

    @Autowired
    public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.objectMapper = new ObjectMapper();
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000); 
        factory.setReadTimeout(3000);   
        
        this.restTemplate = new RestTemplate(factory);
    }

    public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
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
            
            String authResult = authorizeTransaction(transactionId, payerId, payeeId, amount);
            
            if (!"SUCCESS".equals(authResult)) {
                throw new SecurityException("Transferência recusada: " + authResult);
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

    private String authorizeTransaction(UUID transactionId, Long payerId, Long payeeId, BigDecimal amount) {
        String url = "https://desafio.rivs.com.br/api/v1/authorize";

        Map<String, Object> requestBody = new ConcurrentHashMap<>();
        requestBody.put("transactionId", transactionId.toString());
        requestBody.put("payerId", payerId);
        requestBody.put("payeeId", payeeId);
        requestBody.put("amount", amount);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean authorized = (Boolean) response.getBody().get("authorized");
                return (authorized != null && authorized) ? "SUCCESS" : "Não autorizada pelo parceiro";
            }
            return "Erro inesperado na validação";
            
        } catch (HttpClientErrorException.Forbidden e) {
            try {
                String responseBodyString = e.getResponseBodyAsString();
                Map responseBody = objectMapper.readValue(responseBodyString, Map.class);
                if (responseBody != null && responseBody.containsKey("reason")) {
                    return "RISK_ANALYSIS".equals(responseBody.get("reason")) 
                            ? "Reprovado na Análise de Risco" 
                            : responseBody.get("reason").toString();
                }
            } catch (Exception ex) { }
            return "Negada por suspeita de fraude";
            
        } catch (HttpServerErrorException.InternalServerError e) {
            try {
                String responseBodyString = e.getResponseBodyAsString();
                Map responseBody = objectMapper.readValue(responseBodyString, Map.class);
                if (responseBody != null && responseBody.containsKey("message")) {
                    return responseBody.get("message").toString();
                }
            } catch (Exception ex) { }
            return "Falha temporária no sistema autorizador externo";
            
        } catch (ResourceAccessException e) {
            return "Tempo limite esgotado ao consultar o autorizador";
            
        } catch (Exception e) {
            return "Serviço de autorização indisponível no momento";
        }
    }
}