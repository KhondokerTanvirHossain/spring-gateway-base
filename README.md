# spring-gateway-base

A comprehensive Spring Cloud Gateway example repository featuring essential gateway capabilities for modern microservices architectures.

## Features

- Dynamic Routing
- Security (OIDC/JWT Authentication & Authorization)
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
- [x] Integrate OIDC authentication (via Spring Authorization Server)
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

## Overall Architecture & Auth Flow

```
[Browser]
   |
   v
[Gateway:8000] <----> [Client:8999]
   |
   v
[Auth Server:9000]
```

- **Gateway (`:8000`)**: All requests (including authentication) go through here.
- **Client (`:8999`)**: OIDC client, exposes protected resources.
- **Auth Server (`:9000`)**: OIDC provider (Spring Authorization Server), `/auth` context path.

### Request & Authentication Flow

1. **User accesses** a protected route (e.g., `http://localhost:8000/client`) via the gateway.
2. **Gateway** proxies the request to the client app (`:8999`).
3. **Client app** detects the user is not authenticated and starts the OIDC login flow.
4. **OIDC login/authorize endpoints** are routed by the gateway to the auth server (`:9000/auth`).
5. **User logs in** via the auth server (proxied by the gateway).
6. **After successful login**, the user is redirected back to the client (again, via the gateway).
7. **Client app** receives the OIDC ID token and grants access.

**Why route auth through the gateway?**
- Ensures a single entry point for all authentication and API traffic.
- Centralizes session/cookie management and security policies.
- Hides internal service ports from the public.
- Enables consistent logging, tracing, and observability.

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

        - id: auth-server
          uri: http://localhost:9000
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=0

        - id: client-server
          uri: http://localhost:8999
          predicates:
            - Path=/client/**
          filters:
            - StripPrefix=1
```

- Requests to `http://localhost:8070/api/v1/program/client/task/list` are forwarded to `http://localhost:8080/api/v1/program/client/task/list`.
- Requests to `/auth/**` are forwarded to the authorization server (`:9000`) **without** stripping the `/auth` prefix.
- Requests to `/client/**` are forwarded to the client app (`:8999`) **with** the `/client` prefix stripped.

---

## OIDC Auth Configuration (Summary)

**Gateway**: No special OIDC config, just routes `/auth/**` and `/client/**` as above.

**Auth Server (`application.properties`):**
```properties
server.port=9000
server.servlet.context-path=/auth
spring.security.oauth2.authorizationserver.issuer=http://localhost:8000/auth
server.forward-headers-strategy=native
```

**Client App (`application.properties`):**
```properties
server.port=8999
spring.security.oauth2.client.provider.client-oidc.issuer-uri=http://localhost:8000/auth
spring.security.oauth2.client.registration.client-oidc.redirect-uri=http://localhost:8000/client/login/oauth2/code/client-oidc
```

- All OIDC endpoints and redirects use the gateway’s port and context.

---

## Compatibility

Spring Boot: 3.4.2  
Spring Cloud: 2024.0.1

---

## Why This Setup?

- **Security**: All authentication and API traffic is centralized and protected.
- **Simplicity**: Clients and users only interact with the gateway.
- **Scalability**: Easily add more services behind the gateway.
- **Observability**: Centralized logging and tracing for all flows.

---

For more details, see the individual service READMEs and configuration files.