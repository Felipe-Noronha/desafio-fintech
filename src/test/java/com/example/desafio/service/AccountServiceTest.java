package com.example.desafio.service;

import com.example.desafio.model.Account;
import com.example.desafio.model.Transaction;
import com.example.desafio.model.User;
import com.example.desafio.repository.AccountRepository;
import com.example.desafio.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks private AccountService accountService;

    private User clienteLogado;
    private User adminLogado;

    @BeforeEach
    void setUp() {
        clienteLogado = new User();
        clienteLogado.setId(1L);
        clienteLogado.setRole("CUSTOMER");

        adminLogado = new User();
        adminLogado.setId(99L);
        adminLogado.setRole("ADMIN");

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockUsuarioLogado(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
    }

    @Test
    @DisplayName("Deve criar conta com saldo zero se o saldo inicial for nulo")
    void deveCriarContaComSaldoZero() {
        mockUsuarioLogado(clienteLogado);

        Account accountInput = new Account(null, 1L, null);
        Account accountSaved = new Account(1L, 1L, BigDecimal.ZERO);

        when(accountRepository.save(any(Account.class))).thenReturn(accountSaved);

        Account result = accountService.createAccount(accountInput);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(accountRepository, times(1)).save(accountInput);
    }

    @Test
    @DisplayName("Cliente deve consultar a própria conta com sucesso")
    void customerDeveConsultarPropriaConta() {
        mockUsuarioLogado(clienteLogado);
        Account conta = new Account(1L, 1L, BigDecimal.TEN);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(conta));

        Account result = accountService.getAccountById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Cliente deve receber exception ao tentar consultar conta alheia")
    void customerDeveBarrarContaAlheia() {
        mockUsuarioLogado(clienteLogado);
        
        Account contaAlheia = new Account(2L, 2L, BigDecimal.TEN);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(contaAlheia));

        assertThrows(SecurityException.class, () -> {
            accountService.getAccountById(2L);
        });
    }

    @Test
    @DisplayName("Cliente deve encerrar sua própria conta se o saldo for zero")
    void customerDeveEncerrarPropriaContaComSaldoZero() {
        mockUsuarioLogado(clienteLogado);
        Account conta = new Account(1L, 1L, BigDecimal.ZERO);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(conta));

        assertDoesNotThrow(() -> accountService.closeAccount(1L));
        verify(accountRepository, times(1)).delete(conta);
    }

    @Test
    @DisplayName("Cliente deve receber exception ao tentar encerrar conta com saldo positivo")
    void customerNaoDeveEncerrarContaComSaldo() {
        mockUsuarioLogado(clienteLogado);
        Account conta = new Account(1L, 1L, new BigDecimal("100.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(conta));

        assertThrows(IllegalStateException.class, () -> {
            accountService.closeAccount(1L);
        });

        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    @DisplayName("Admin deve conseguir encerrar qualquer conta com saldo zero")
    void adminDeveEncerrarQualquerConta() {
        mockUsuarioLogado(adminLogado);
        Account contaAlheia = new Account(2L, 2L, BigDecimal.ZERO);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(contaAlheia));

        assertDoesNotThrow(() -> accountService.closeAccount(2L));
        verify(accountRepository, times(1)).delete(contaAlheia);
    }
    
    @Test
    @DisplayName("Admin deve conseguir criar conta definindo saldo inicial positivo")
    void adminDeveCriarContaComSaldoInicial() {
        mockUsuarioLogado(adminLogado);

        Account accountInput = new Account(null, 5L, new BigDecimal("250.00"));
        Account accountSaved = new Account(12L, 5L, new BigDecimal("250.00"));

        when(accountRepository.save(any(Account.class))).thenReturn(accountSaved);

        Account result = accountService.createAccount(accountInput);

        assertNotNull(result);
        assertEquals(new BigDecimal("250.00"), result.getBalance());
        verify(accountRepository, times(1)).save(accountInput);
    }

    @Test
    @DisplayName("Deve obter o extrato paginado se a conta existir e pertencer ao cliente")
    void deveRetornarExtratoComSucesso() {
        mockUsuarioLogado(clienteLogado);
        
        Account conta = new Account(1L, 1L, BigDecimal.TEN);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(conta));
        
        Transaction tx = new Transaction(UUID.randomUUID(), 1L, 2L, BigDecimal.TEN, LocalDateTime.now(), "SUCCESS");
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionRepository.findLatestTransactions(1L, pageable)).thenReturn(Collections.singletonList(tx));

        List<Transaction> result = accountService.getAccountStatement(1L, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findLatestTransactions(1L, pageable);
    }
}