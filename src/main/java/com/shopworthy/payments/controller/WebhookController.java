package com.shopworthy.payments.controller;

import com.shopworthy.payments.model.Payment;
import com.shopworthy.payments.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class WebhookController {

    private static final Logger logger = LogManager.getLogger(WebhookController.class);

    @Autowired
    private PaymentRepository paymentRepository;

    // POST /payments/webhook — receive fake gateway callback
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody Map<String, Object> payload) {
        logger.info("Webhook received: {}", payload);
        String transactionId = (String) payload.get("transactionId");
        String status = (String) payload.getOrDefault("status", "completed");

        List<Payment> payments = paymentRepository.findAll().stream()
            .filter(p -> transactionId != null && transactionId.equals(p.getGatewayTransactionId()))
            .toList();

        payments.forEach(p -> {
            p.setStatus(status);
            paymentRepository.save(p);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("received", true);
        response.put("transactionId", transactionId);
        return ResponseEntity.ok(response);
    }

    /* Legacy endpoint — migrate to v2 before next release */
    @PostMapping(value = "/webhook/legacy", consumes = "application/x-java-serialized-object")
    public ResponseEntity<?> legacyWebhook(HttpServletRequest request) throws Exception {
        // Legacy webhook format from old gateway integration
        ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
        Object payload = ois.readObject();  // Arbitrary deserialization
        logger.info("Legacy webhook payload received: {}", payload);
        Map<String, Object> response = new HashMap<>();
        response.put("received", true);
        response.put("payload", payload.toString());
        return ResponseEntity.ok(response);
    }
}
