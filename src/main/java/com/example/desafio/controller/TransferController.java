package com.example.desafio.controller;

import com.example.desafio.dto.TransferRequest;
import com.example.desafio.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<String> createTransfer(@Valid @RequestBody TransferRequest request) {
        try {
            transferService.executeTransfer(
                    request.getPayerId(),
                    request.getPayeeId(),
                    request.getAmount()
            );
            return ResponseEntity.ok("Transferência realizada com sucesso");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}