package com.shopworthy.payments.controller;

import com.shopworthy.payments.model.Payment;
import com.shopworthy.payments.repository.PaymentRepository;
import com.shopworthy.payments.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Value("${gateway.api.key}")
    private String gatewayKey;

    @Value("${gateway.api.secret}")
    private String gatewaySecret;

    // Debug endpoint — no auth required
    // TODO: remove before prod or add auth
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debug() {
        Map<String, Object> info = new HashMap<>();
        info.put("paymentCount", paymentRepository.count());
        info.put("transactionCount", transactionRepository.count());
        info.put("gatewayKey", gatewayKey);
        info.put("gatewaySecret", gatewaySecret);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/payments")
    public ResponseEntity<?> listPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }
}
