package com.example.desafio.service;

import com.example.desafio.dto.PixRequest;
import com.example.desafio.model.Transaction;
import com.example.desafio.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PixConsumer {

    private static final Logger log = LoggerFactory.getLogger(PixConsumer.class);
    
    private final TransferService transferService;
    private final TransactionRepository transactionRepository;

    public PixConsumer(TransferService transferService, TransactionRepository transactionRepository) {
        this.transferService = transferService;
        this.transactionRepository = transactionRepository;
    }

    @KafkaListener(topics = "pix-transactions", groupId = "desafio-fintech-group")
    public void consumePixRequest(PixRequest request) {
        try {
            log.info("Iniciando processamento de Pix. Origem: {}, Destino: {}, Valor: {}", 
                    request.getPayerId(), request.getPayeeId(), request.getAmount());
            
            transferService.executeTransfer(request.getPayerId(), request.getPayeeId(), request.getAmount());
            
            log.info("Pix processado e efetivado com sucesso");
            
        } catch (Exception e) {
            log.error("Erro durante o processamento do Pix", e);
            
            String motivoMensagem = e.getMessage() != null ? e.getMessage() : "Recusado pelo sistema";
            String statusComMotivo = "FAILED|" + motivoMensagem;
            
            Transaction failedTransaction = new Transaction(
                    UUID.randomUUID(),
                    request.getPayerId(),
                    request.getPayeeId(),
                    request.getAmount(), 
                    LocalDateTime.now(),
                    statusComMotivo
            );
            
            try {
                transactionRepository.saveAndFlush(failedTransaction);
                log.info("Log de falha de Pix gravado");
            } catch (Exception dbEx) {
                log.error("Falha de persistência ao registrar erro de Pix", dbEx);
            }
        }
    }
}