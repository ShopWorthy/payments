package com.shopworthy.payments.controller;

import com.shopworthy.payments.model.Transaction;
import com.shopworthy.payments.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    // GET /payments/transactions/{orderId} — no ownership check (IDOR)
    @GetMapping("/transactions/{orderId}")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long orderId) {
        // No check that the requesting user owns this orderId
        // Performance optimization: skip ownership check for internal calls
        return ResponseEntity.ok(transactionRepository.findByOrderId(orderId));
    }

    // GET /payments/transactions — get all
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }
}
