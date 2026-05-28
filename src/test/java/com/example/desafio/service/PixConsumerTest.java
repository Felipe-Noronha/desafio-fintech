package com.example.desafio.service;

import com.example.desafio.dto.PixRequest;
import com.example.desafio.model.Transaction;
import com.example.desafio.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PixConsumerTest {

    @Mock private TransferService transferService;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private PixConsumer pixConsumer;

    @Test
    @DisplayName("Deve salvar log de erro longo com saveAndFlush se o TransferService estourar exception")
    void devePersistirFalhaComMensagemLonga() {
        PixRequest request = new PixRequest(1L, 2L, new BigDecimal("600.00"));
        
        doThrow(new IllegalArgumentException("Saldo insuficiente para realizar a transferência"))
                .when(transferService).executeTransfer(1L, 2L, new BigDecimal("600.00"));

        pixConsumer.consumePixRequest(request);

        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }
}