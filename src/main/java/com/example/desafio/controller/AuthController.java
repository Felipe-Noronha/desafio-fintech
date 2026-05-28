package com.example.desafio.controller;

import com.example.desafio.model.User;
import com.example.desafio.repository.UserRepository;
import com.example.desafio.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); 

    public AuthController(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Senha inválida"));
        }

        String token = tokenService.generateToken(user);

        Map<String, String> response = new ConcurrentHashMap<>();
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }
}