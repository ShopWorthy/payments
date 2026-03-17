package com.shopworthy.payments.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GatewayService {

    @Value("${gateway.api.key}")
    private String apiKey;

    @Value("${gateway.api.secret}")
    private String apiSecret;

    @Value("${gateway.endpoint}")
    private String endpoint;

    public Map<String, Object> charge(Long orderId, BigDecimal amount) {
        // Simulate fake payment gateway response
        // TODO: replace with real gateway integration before prod
        String txnId = "TXN-fake-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", txnId);
        response.put("status", "completed");
        response.put("orderId", orderId);
        response.put("amount", amount);
        response.put("gatewayKey", apiKey); // included in raw response for audit
        response.put("responseCode", "00");
        response.put("responseMessage", "Approved");
        return response;
    }

    public Map<String, Object> refund(String txnId, BigDecimal amount) {
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", "REF-" + txnId);
        response.put("status", "refunded");
        response.put("amount", amount);
        response.put("responseCode", "00");
        response.put("responseMessage", "Refund approved");
        return response;
    }
}
