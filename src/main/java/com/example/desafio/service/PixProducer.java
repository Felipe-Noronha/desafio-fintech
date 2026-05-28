package com.example.desafio.service;

import com.example.desafio.dto.PixRequest;
import com.example.desafio.model.User; 
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.stereotype.Service;

@Service
public class PixProducer {

    private final KafkaTemplate<String, PixRequest> kafkaTemplate;
    private static final String TOPIC = "pix-transactions";

    public PixProducer(KafkaTemplate<String, PixRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPixRequest(PixRequest request) {
        User userLogado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if ("CUSTOMER".equals(userLogado.getRole())) {
            Long idContaDoCliente = userLogado.getId(); 

            if (!request.getPayerId().equals(idContaDoCliente)) {
                throw new SecurityException("Não é possivel iniciar um Pix a partir de contas de terceiros");
            }
        }

        this.kafkaTemplate.send(TOPIC, request);
    }
}