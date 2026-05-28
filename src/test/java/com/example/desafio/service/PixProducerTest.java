package com.example.desafio.service;

import com.example.desafio.dto.PixRequest;
import com.example.desafio.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PixProducerTest {

    @Mock private KafkaTemplate<String, PixRequest> kafkaTemplate;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks private PixProducer pixProducer;

    private User clienteLogado;

    @BeforeEach
    void setUp() {
        clienteLogado = new User();
        clienteLogado.setId(1L);
        clienteLogado.setRole("CUSTOMER");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Deve enviar requisição de Pix para o tópico do Kafka se o pagador for o próprio dono")
    void deveEnviarPixComSucesso() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(clienteLogado);

        PixRequest request = new PixRequest(1L, 2L, BigDecimal.TEN);

        assertDoesNotThrow(() -> pixProducer.sendPixRequest(request));
        verify(kafkaTemplate, times(1)).send(eq("pix-transactions"), eq(request));
    }

    @Test
    @DisplayName("Deve lançar exception se o cliente tentar emitir um Pix tirando dinheiro de outra conta")
    void deveBarrarPixDeTerceiros() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(clienteLogado);

        PixRequest request = new PixRequest(2L, 1L, BigDecimal.TEN);

        assertThrows(SecurityException.class, () -> pixProducer.sendPixRequest(request));
        verify(kafkaTemplate, never()).send(anyString(), any(PixRequest.class));
    }
}