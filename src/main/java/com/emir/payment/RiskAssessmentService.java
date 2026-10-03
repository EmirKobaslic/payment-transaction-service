package com.emir.payment.transaction;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class RiskAssessmentService {
    private static final BigDecimal REVIEW_THRESHOLD = new BigDecimal("1000.00");

    public RiskLevel assess(BigDecimal amount) {
        if (amount.compareTo(REVIEW_THRESHOLD) >= 0) {
            return RiskLevel.HIGH;
        }

        return RiskLevel.LOW;
    }
}