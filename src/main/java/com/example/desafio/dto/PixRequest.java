package com.example.desafio.dto;

import java.math.BigDecimal;

public class PixRequest {
    private Long payerId;
    private Long payeeId;
    private BigDecimal amount;

    public PixRequest() {}

    public PixRequest(Long payerId, Long payeeId, BigDecimal amount) {
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
    }

    public Long getPayerId() { return payerId; }
    public void setPayerId(Long payerId) { this.payerId = payerId; }
    public Long getPayeeId() { return payeeId; }
    public void setPayeeId(Long payeeId) { this.payeeId = payeeId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}