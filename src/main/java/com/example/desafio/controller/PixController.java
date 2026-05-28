package com.example.desafio.controller;

import com.example.desafio.dto.PixRequest;
import com.example.desafio.service.PixProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/pix")
public class PixController {

    private final PixProducer pixProducer;

    public PixController(PixProducer pixProducer) {
        this.pixProducer = pixProducer;
    }

    @PostMapping
    public ResponseEntity<String> initiatePix(@Valid @RequestBody PixRequest request) {
        try {
            pixProducer.sendPixRequest(request);
            return ResponseEntity.accepted().body("Pedido de Pix recebido e enviado para processamento");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}