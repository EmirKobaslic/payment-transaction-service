package com.emir.payment.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransactionService {
    private final TransactionRepository repository;
    private final RiskAssessmentService riskAssessmentService;

    public TransactionService(
        TransactionRepository repository,
        RiskAssessmentService riskAssessmentService
    ) {
        this.repository = repository;
        this.riskAssessmentService = riskAssessmentService;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        RiskLevel riskLevel = 
            riskAssessmentService.assess(request.amount());

        TransactionStatus status = riskLevel == RiskLevel.HIGH 
            ? TransactionStatus.REVIEW 
            : TransactionStatus.APPROVED;

        Transaction transaction = new Transaction(
            request.amount(),
            request.currency(),
            riskLevel,
            status
        );

        Transaction saved = repository.save(transaction);

        return TransactionResponse.from(saved);
    }

    public Optional<TransactionResponse> findById(UUID id) {
        return repository.findById(id)
            .map(TransactionResponse::from);
    }

    public List<TransactionResponse> findAll(TransactionStatus status) {
        List<Transaction> transaction = status == null
            ? repository.findAll()
            : repository.findByStatus(status);

        return transaction.stream()
            .map(TransactionResponse::from)
            .toList();
    }



}