package com.example.desafio.controller;

import com.example.desafio.model.User;
import com.example.desafio.repository.UserRepository;
import com.example.desafio.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; 
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private TokenService tokenService;

    @InjectMocks private AuthController authController;

    private User usuarioMock;
    
    private final PasswordEncoder encoderReal = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        usuarioMock = new User();
        usuarioMock.setEmail("teste@dunnas.com");
        
        usuarioMock.setPassword(encoderReal.encode("123"));
        usuarioMock.setRole("CUSTOMER");
    }

    @Test
    @DisplayName("Deve autenticar usuário e retornar token com sucesso")
    void deveAutenticarComSucesso() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "teste@dunnas.com");
        loginRequest.put("password", "123"); 

        when(userRepository.findByEmail("teste@dunnas.com")).thenReturn(Optional.of(usuarioMock));
        when(tokenService.generateToken(usuarioMock)).thenReturn("token-jwt-valido");

        ResponseEntity<Map<String, String>> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("token-jwt-valido", response.getBody().get("token"));
        assertEquals("CUSTOMER", response.getBody().get("role"));
        assertEquals("teste@dunnas.com", response.getBody().get("email"));
    }

    @Test
    @DisplayName("Deve retornar 401 se a senha fornecida for incorreta")
    void deveRetornar401ParaSenhaIncorreta() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "teste@dunnas.com");
        loginRequest.put("password", "senha_errada"); 

        when(userRepository.findByEmail("teste@dunnas.com")).thenReturn(Optional.of(usuarioMock));

        ResponseEntity<Map<String, String>> response = authController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Senha inválida", response.getBody().get("message"));
        verify(tokenService, never()).generateToken(any(User.class));
    }
}