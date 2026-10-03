package com.emir.payment.transaction;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
        @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionResponse response = service.create(request);

        URI location = URI.create(
            "/api/transactions/" + response.id()
        );

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
        @PathVariable UUID id 
    ) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<TransactionResponse> findAll(
        @RequestParam(required = false) TransactionStatus status
    ) {
        return service.findAll(status);
    }
}