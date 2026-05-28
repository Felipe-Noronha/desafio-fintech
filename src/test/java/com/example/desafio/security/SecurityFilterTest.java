package com.example.desafio.security;

import com.example.desafio.model.User;
import com.example.desafio.repository.UserRepository;
import com.example.desafio.service.SecurityFilter;
import com.example.desafio.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock private TokenService tokenService;
    @Mock private UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private SecurityFilter securityFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar a requisição se encontrar um Cookie Token válido")
    void deveAutenticarComCookieValido() throws Exception {
        Cookie authCookie = new Cookie("AUTH_TOKEN", "jwt-valido");
        Cookie[] cookies = { authCookie };

        User user = new User();
        user.setEmail("usuario@email.com");
        user.setRole("CUSTOMER");

        when(request.getCookies()).thenReturn(cookies);
        when(tokenService.validateToken("jwt-valido")).thenReturn("usuario@email.com");
        when(userRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(user));

        securityFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals("usuario@email.com", principal.getEmail());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar se o Cookie Token for inválido ou estiver expirado")
    void naoDeveAutenticarComCookieInvalido() throws Exception {
        Cookie authCookie = new Cookie("AUTH_TOKEN", "jwt-expirado");
        Cookie[] cookies = { authCookie };

        when(request.getCookies()).thenReturn(cookies);
        when(tokenService.validateToken("jwt-expirado")).thenReturn(null);

        securityFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).findByEmail(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}