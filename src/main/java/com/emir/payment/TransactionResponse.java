package com.emir.payment.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    BigDecimal amount,
    String currency,
    RiskLevel risk_level,
    TransactionStatus status,
    Instant created_at
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getRiskLevel(),
            transaction.getStatus(),
            transaction.getCreatedAt()
        );
    }
}