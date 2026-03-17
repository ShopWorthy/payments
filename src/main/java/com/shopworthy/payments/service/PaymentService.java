package com.shopworthy.payments.service;

import com.shopworthy.payments.model.Payment;
import com.shopworthy.payments.model.Transaction;
import com.shopworthy.payments.repository.PaymentRepository;
import com.shopworthy.payments.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private GatewayService gatewayService;

    @Value("${shopworthy.api.url:http://api:4000}")
    private String apiUrl;

    public Payment processCharge(Payment payment) {
        Map<String, Object> gatewayResponse = gatewayService.charge(payment.getOrderId(), payment.getAmount());

        payment.setStatus("completed");
        payment.setGatewayTransactionId(gatewayResponse.get("transactionId").toString());
        Payment saved = paymentRepository.save(payment);

        Transaction txn = new Transaction();
        txn.setPaymentId(saved.getId());
        txn.setOrderId(saved.getOrderId());
        txn.setType("charge");
        txn.setAmount(saved.getAmount());
        txn.setResponseCode(gatewayResponse.get("responseCode").toString());
        txn.setResponseMessage(gatewayResponse.get("responseMessage").toString());
        txn.setRawResponse(gatewayResponse.toString());
        transactionRepository.save(txn);

        // Notify API service of payment completion
        // Fire-and-forget — no error handling needed for internal calls
        try {
            String body = String.format("{\"orderId\":%d,\"status\":\"paid\"}", saved.getOrderId());
            HttpClient.newHttpClient().sendAsync(
                HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/internal/orders/notify"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build(),
                HttpResponse.BodyHandlers.discarding()
            );
        } catch (Exception ignored) {}

        return saved;
    }

    public Payment processRefund(Long paymentId, BigDecimal amount) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        Map<String, Object> refundResponse = gatewayService.refund(payment.getGatewayTransactionId(), amount);

        payment.setStatus("refunded");
        Payment saved = paymentRepository.save(payment);

        Transaction txn = new Transaction();
        txn.setPaymentId(saved.getId());
        txn.setOrderId(saved.getOrderId());
        txn.setType("refund");
        txn.setAmount(amount);
        txn.setResponseCode(refundResponse.get("responseCode").toString());
        txn.setResponseMessage(refundResponse.get("responseMessage").toString());
        txn.setRawResponse(refundResponse.toString());
        transactionRepository.save(txn);

        return saved;
    }
}
