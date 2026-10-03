package com.emir.payment.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository repository;

    @Test
    void createsAndRetrievesLowRiskTransaction() throws Exception {
        long initialCount = repository.count();

        var result = mockMvc.perform(post("/api/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
            {
                "amount": 125.50,
                "currency": "EUR"
            }
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.risk_level").value("LOW"))
        .andReturn();

        assertEquals(initialCount + 1, repository.count());

        String location = result.getResponse().getHeader("Location");

        assertNotNull(location);

        assertTrue(location.startsWith("/api/transactions/"));

        mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(125.50))
            .andExpect(jsonPath("$.currency").value("EUR"))
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void highAmountRequiresReview() throws Exception {
        var result = mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                        "amount": 1200.00,
                        "currency": "EUR"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.risk_level").value("HIGH"))
                .andExpect(jsonPath("$.status").value("REVIEW"))
                .andReturn();
    }

    @Test
    void rejectsNegativeAmountWithoutSaving() throws Exception {
        long initialCount = repository.count();
        var result = mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                        "amount": -1200.00,
                        "currency": "EUR"
                    }
                    """))
                .andExpect(status().isBadRequest());

        assertEquals(initialCount, repository.count());
    }

    @Test
    void returnsOnlyTransactionsRequiringReview() throws Exception {
        repository.save(new Transaction(
            new BigDecimal("1500.00"),
            "EUR",
            RiskLevel.HIGH,
            TransactionStatus.REVIEW
        ));

        repository.save(new Transaction(
            new BigDecimal("150.00"),
            "EUR",
            RiskLevel.LOW,
            TransactionStatus.APPROVED
        ));

        var resp = mockMvc.perform(get("/api/transactions")
                .param("status", "REVIEW"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].status").value("REVIEW"))
            .andExpect(jsonPath("$[0].amount").value(1500.00));

        
    }

    @Test
    void missingTransactionReturnsNotFound() throws Exception
     {
        mockMvc.perform(get(
            "/api/transactions/00000000-0000-0000-0000-000000000000"
        ))
        .andExpect(status().isNotFound());
     }
}