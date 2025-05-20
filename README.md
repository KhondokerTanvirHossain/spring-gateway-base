# spring-gateway-base

A comprehensive Spring Cloud Gateway example repository featuring essential gateway capabilities for modern microservices architectures.

## Features

- Dynamic Routing
- Security (JWT Authentication & Authorization)
- Circuit Breaker
- Logging
- Rate Limiting
- Caching
- Feature Flags (Enable/Disable Capabilities)

---

## Roadmap

### 1. Project Setup
- [ ] Initialize Spring Boot project with Spring Cloud Gateway
- [ ] Configure basic routing

### 2. Security
- [ ] Integrate JWT authentication
- [ ] Implement role-based authorization

### 3. Circuit Breaker
- [ ] Add Resilience4j circuit breaker
- [ ] Configure fallback routes

### 4. Logging
- [ ] Implement request/response logging
- [ ] Add correlation IDs for tracing

### 5. Rate Limiting
- [ ] Integrate rate limiting (Redis/Memory)
- [ ] Configure per-route rate limits

### 6. Caching
- [ ] Add response caching for selected routes

### 7. Feature Flags
- [ ] Integrate feature flag management (e.g., Togglz, FF4J)
- [ ] Enable/disable gateway capabilities via flags

### 8. Documentation & Examples
- [ ] Add usage examples for each feature
- [ ] Provide configuration samples

---

## Getting Started

1. Clone the repository
2. Follow the roadmap to implement features step by step

---

## Example: Routing Requests

The API Gateway is configured to route requests to your microservices.  
For example, to route the following API:

```bash
curl --location 'http://localhost:8070/api/v1/program/client/task/list'
```

The `application.yml` contains:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: program-task-service
          uri: http://localhost:8080
          predicates:
            - Path=/api/v1/program/client/task/list
```

Requests to `http://localhost:8070/api/v1/program/client/task/list` are forwarded to `http://localhost:8080/api/v1/program/client/task/list`.

## Compatibility

Spring Boot: 3.4.2
Spring Cloud: 2024.0.1