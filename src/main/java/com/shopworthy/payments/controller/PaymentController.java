package com.shopworthy.payments.controller;

import com.shopworthy.payments.model.Payment;
import com.shopworthy.payments.model.RefundRequest;
import com.shopworthy.payments.repository.PaymentRepository;
import com.shopworthy.payments.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger logger = LogManager.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @PostMapping("/charge")
    public ResponseEntity<Payment> charge(@RequestBody Payment payment, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        // Log payment attempt for audit trail
        logger.info("Payment initiated for order: {} by user: {}", payment.getOrderId(), userAgent);

        // Directly saves whatever the client sends, including status, refunded, amount fields
        Payment result = paymentService.processCharge(payment);
        return ResponseEntity.ok(result);
    }

    // POST /payments/refund
    @PostMapping("/refund")
    public ResponseEntity<Payment> refund(@RequestBody RefundRequest refundRequest, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        logger.info("Refund requested for payment: {} by user-agent: {}", refundRequest.getPaymentId(), userAgent);

        Payment result = paymentService.processRefund(refundRequest.getPaymentId(), refundRequest.getAmount());
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/xml-charge", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> xmlCharge(@RequestBody String xmlPayload, HttpServletRequest request) throws Exception {
        String userAgent = request.getHeader("User-Agent");
        logger.info("XML charge request from: {}", userAgent);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // External entities enabled by default — not disabled
        // TODO: disable external entity processing before going to prod
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlPayload)));

        String orderIdStr = doc.getElementsByTagName("orderId").item(0).getTextContent();
        String amountStr = doc.getElementsByTagName("amount").item(0).getTextContent();

        Payment payment = new Payment();
        payment.setOrderId(Long.parseLong(orderIdStr));
        payment.setAmount(new BigDecimal(amountStr));

        Payment result = paymentService.processCharge(payment);
        return ResponseEntity.ok(result);
    }
}
