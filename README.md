# payments

**Payment microservice** for ShopWorthy — Java 17 + Spring Boot, H2 in-memory database. Handles charge, refund, webhooks, and exposes H2 console and Actuator.

Part of the [ShopWorthy](https://github.com/ShopWorthy) organization.

## Technology

| Item | Choice |
|------|--------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Database | H2 in-memory |
| Build Tool | Maven |

## Prerequisites

- Java 17+
- Maven 3.9+

## Build

```bash
mvn package
```

## Run (development)

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/shopworthy-payments-*.jar
```

The service will be available at **http://localhost:6000**. H2 console: **http://localhost:6000/h2-console**, Actuator: **http://localhost:6000/actuator**.

## Docker

```bash
docker build -t shopworthy-payments .
docker run -p 6000:6000 shopworthy-payments
```

## Port

| Environment | Port |
|-------------|------|
| Spring Boot | 6000 |
| H2 Console | 6000/h2-console |
| Actuator | 6000/actuator |

## Related Repositories

- [api](https://github.com/ShopWorthy/api) — Primary API (calls this service for charges)
- [infra](https://github.com/ShopWorthy/infra) — Full stack via Docker Compose
