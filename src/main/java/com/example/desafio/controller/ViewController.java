package com.example.desafio.controller;

import com.example.desafio.model.Account;
import com.example.desafio.model.Transaction;
import com.example.desafio.model.User;
import com.example.desafio.service.AccountService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class ViewController {

    private final AccountService accountService;
    
    public ViewController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Model model, @RequestParam(defaultValue = "10") int limit) {
        User userLogado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        try {
            Account account = accountService.getAccountById(userLogado.getId());
            List<Transaction> transactions = accountService.getAccountStatement(account.getId(), limit);
            
            model.addAttribute("user", userLogado);
            model.addAttribute("account", account);
            model.addAttribute("transactions", transactions);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Você ainda não possui uma conta ativa. Vá em Gerenciar Contas para abrir uma");
        } catch (Exception e) {
            model.addAttribute("error", "Erro inesperado ao carregar dados: " + e.getMessage());
        }
        
        return "dashboard";
    }

    @GetMapping("/transfer")
    public String transferPage(Model model) {
        User userLogado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("userId", userLogado.getId());
        return "transfer";
    }
    
    @GetMapping("/accounts/manage")
    public String manageAccountsPage(Model model) {
        User userLogado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        model.addAttribute("userId", userLogado.getId());
        model.addAttribute("userRole", userLogado.getRole()); 
        
        try {
            Account account = accountService.getAccountById(userLogado.getId());
            model.addAttribute("account", account);
        } catch (IllegalArgumentException e) {
            model.addAttribute("account", null);
        }
        
        return "accounts";
    }
}