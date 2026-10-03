package com.emir.payment.transaction;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskAssessmentServiceTest {
    private final RiskAssessmentService service = new RiskAssessmentService();

    @Test
    void belowThresholdHasLowRisk() {
        RiskLevel result = service.assess(new BigDecimal("999.99"));

        assertEquals(RiskLevel.LOW, result);
    }

    @Test 
    void atThresholdHasHighRisk() {
        RiskLevel result = service.assess(new BigDecimal("1000.00"));

        assertEquals(RiskLevel.HIGH, result);
    }

    @Test
    void aboveThresholdHasHighRisk() {
        RiskLevel result = service.assess(new BigDecimal("1200.00"));

        assertEquals(RiskLevel.HIGH, result);
    }
}