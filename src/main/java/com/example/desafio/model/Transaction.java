package com.example.desafio.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_transaction")
public class Transaction {

    @Id
    private UUID id;

    @Column(name = "payer_account_id", nullable = false)
    private Long payerAccountId;

    @Column(name = "payee_account_id", nullable = false)
    private Long payeeAccountId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 255)
    private String status;

    public Transaction() {}

    public Transaction(UUID id, Long payerAccountId, Long payeeAccountId, BigDecimal amount, LocalDateTime createdAt, String status) {
        this.id = id;
        this.payerAccountId = payerAccountId;
        this.payeeAccountId = payeeAccountId;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getPayerAccountId() { return payerAccountId; }
    public void setPayerAccountId(Long payerAccountId) { this.payerAccountId = payerAccountId; }

    public Long getPayeeAccountId() { return payeeAccountId; }
    public void setPayeeAccountId(Long payeeAccountId) { this.payeeAccountId = payeeAccountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}