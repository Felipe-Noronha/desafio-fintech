package com.example.desafio.config;

import com.example.desafio.model.User;
import com.example.desafio.model.Account;
import com.example.desafio.repository.UserRepository;
import com.example.desafio.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            
            User cliente1 = new User();
            cliente1.setName("Cliente");
            cliente1.setEmail("cliente@email.com");
            cliente1.setPassword(encoder.encode("123")); 
            cliente1.setRole("CUSTOMER");
            cliente1 = userRepository.save(cliente1);

            Account conta1 = new Account();
            conta1.setUserId(cliente1.getId());
            conta1.setBalance(new BigDecimal("500.00"));
            accountRepository.save(conta1);

            User cliente2 = new User();
            cliente2.setName("Segundo Cliente");
            cliente2.setEmail("cliente2@email.com");
            cliente2.setPassword(encoder.encode("123"));
            cliente2.setRole("CUSTOMER");
            cliente2 = userRepository.save(cliente2);

            Account conta2 = new Account();
            conta2.setUserId(cliente2.getId());
            conta2.setBalance(new BigDecimal("100.00"));
            accountRepository.save(conta2);

            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@email.com");
            admin.setPassword(encoder.encode("123"));
            admin.setRole("ADMIN");
            admin = userRepository.save(admin);

            Account conta3 = new Account();
            conta3.setUserId(admin.getId());
            conta3.setBalance(new BigDecimal("250.00"));
            accountRepository.save(conta3);
            
        }
    }
}