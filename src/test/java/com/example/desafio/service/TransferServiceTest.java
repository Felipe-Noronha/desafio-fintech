package com.example.desafio.service;

import com.example.desafio.model.Account;
import com.example.desafio.model.User;
import com.example.desafio.repository.AccountRepository;
import com.example.desafio.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    private TransferService transferService;

    private User clienteLogado;

    @BeforeEach
    void setUp() {
        clienteLogado = new User();
        clienteLogado.setId(1L);
        clienteLogado.setRole("CUSTOMER");

        SecurityContextHolder.setContext(securityContext);

        transferService = new TransferService(accountRepository, transactionRepository, restTemplate);
    }

    private void mockUsuarioLogado() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(clienteLogado);
    }

    @Test
    @DisplayName("Deve realizar a transferência completa se houver saldo e o serviço externo autorizar")
    void deveTransferirComSucesso() {
        mockUsuarioLogado();

        Account contaOrigem = new Account(1L, 1L, new BigDecimal("500.00"));
        Account contaDestino = new Account(2L, 2L, new BigDecimal("100.00"));

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaOrigem));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));

        Map<String, Object> authBody = new HashMap<>();
        authBody.put("authorized", true);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(authBody, HttpStatus.OK);
        
        lenient().when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(responseEntity);

        assertDoesNotThrow(() -> transferService.executeTransfer(1L, 2L, new BigDecimal("50.00")));

        verify(accountRepository, times(1)).save(contaOrigem);
        verify(accountRepository, times(1)).save(contaDestino);
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve recusar a transferência se o autorizador retornar 403 (RISK_ANALYSIS)")
    void deveRecusarQuandoAutorizadorRetornar403() {
        mockUsuarioLogado();

        Account contaOrigem = new Account(1L, 1L, new BigDecimal("500.00"));
        Account contaDestino = new Account(2L, 2L, new BigDecimal("100.00"));

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaOrigem));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));

        String jsonError = "{\"authorized\": false, \"reason\": \"RISK_ANALYSIS\"}";
        HttpClientErrorException exception403 = new HttpClientErrorException(
                HttpStatus.FORBIDDEN, 
                "Forbidden", 
                jsonError.getBytes(StandardCharsets.UTF_8), 
                StandardCharsets.UTF_8
        );

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenThrow(exception403);

        assertThrows(SecurityException.class, () -> {
            transferService.executeTransfer(1L, 2L, new BigDecimal("50.00"));
        });

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Deve recusar a transferência se o autorizador retornar 500 (Erro Interno)")
    void deveRecusarQuandoAutorizadorRetornar500() {
        mockUsuarioLogado();

        Account contaOrigem = new Account(1L, 1L, new BigDecimal("500.00"));
        Account contaDestino = new Account(2L, 2L, new BigDecimal("100.00"));

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaOrigem));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));

        String jsonError = "{\"message\": \"Temporary authorization failure\"}";
        HttpServerErrorException exception500 = new HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Internal Server Error", 
                jsonError.getBytes(StandardCharsets.UTF_8), 
                StandardCharsets.UTF_8
        );

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenThrow(exception500);

        assertThrows(SecurityException.class, () -> {
            transferService.executeTransfer(1L, 2L, new BigDecimal("50.00"));
        });

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Bloqueio de transferência se a conta de origem não for do próprio dono autenticado")
    void deveBarrarTransferenciaSincronaInvasora() {
        mockUsuarioLogado();

        Account contaInvasoraOrigem = new Account(2L, 2L, BigDecimal.TEN);
        Account contaDestino = new Account(1L, 1L, BigDecimal.ZERO);

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaDestino));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaInvasoraOrigem));

        assertThrows(SecurityException.class, () -> {
            transferService.executeTransfer(2L, 1L, BigDecimal.TEN);
        });
    }

    @Test
    @DisplayName("Pular validação de segurança e rodar locks se a autenticação for nula")
    void deveProcessarKafkaSemAuthenticationContext() {
        when(securityContext.getAuthentication()).thenReturn(null);

        Account conta1 = new Account(1L, 1L, new BigDecimal("100.00"));
        Account conta2 = new Account(2L, 2L, new BigDecimal("50.00"));

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(conta1));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(conta2));

        assertThrows(IllegalArgumentException.class, () -> {
            transferService.executeTransfer(1L, 2L, new BigDecimal("9999.00"));
        });

        verify(accountRepository, times(1)).findByIdWithLock(1L);
        verify(accountRepository, times(1)).findByIdWithLock(2L);
    }

    @Test
    @DisplayName("Rejeitar transferência se a conta de origem e destino forem iguais")
    void deveRejeitarMesmaConta() {
        assertThrows(IllegalArgumentException.class, () -> {
            transferService.executeTransfer(1L, 1L, BigDecimal.TEN);
        });
    }
}