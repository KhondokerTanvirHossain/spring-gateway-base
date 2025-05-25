# Spring Cloud Gateway Base

A comprehensive Spring Cloud Gateway example repository featuring essential gateway capabilities for modern microservices architectures. This project demonstrates how to build a robust API Gateway with routing, security, observability, and extensibility in mind.

---

## Overview

This project is a template for building a production-ready API Gateway using Spring Cloud Gateway. It includes:

- Dynamic routing to microservices
- OIDC authentication routing (with Spring Authorization Server)
- JWT authentication and role-based authorization (planned)
- Circuit breaker and fallback support (planned)
- Request/response logging and distributed tracing
- Rate limiting and caching (planned)
- Feature flags for enabling/disabling gateway features (planned)
- Structured logging with Logback and MDC

---

## Build Configuration

### `build.gradle`

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.2'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.tanvir'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

ext {
    set('springCloudVersion', "2024.0.1")
}

dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.projectreactor:reactor-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'org.testng:testng:7.7.0'
    implementation 'org.modelmapper:modelmapper:3.1.1'
    testImplementation 'com.h2database:h2:2.2.224'
    implementation "org.graalvm.buildtools:native-gradle-plugin:0.10.3"

    implementation 'io.micrometer:micrometer-tracing-bridge-brave'
    implementation 'io.zipkin.reporter2:zipkin-reporter-brave'

    implementation 'net.logstash.logback:logstash-logback-encoder:7.4'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

> **Note:** Spring Boot 3.4.x is not compatible with Spring Cloud 2023.0.x or earlier. If you encounter compatibility issues, downgrade to Spring Boot 3.3.x and Spring Cloud 2023.0.x.

---

## Gateway Routing Configuration

### `src/main/resources/application.yml`

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

## OIDC Authentication Routing (Auth Details for Gateway)

### Why Route OIDC Flows Through the Gateway?

Routing all authentication (OIDC) flows through the gateway ensures:
- A single entry point for all clients and authentication.
- Consistent session and cookie handling.
- No direct exposure of the authorization server to the public.

### How It Works

1. **User visits** `http://localhost:8000/client` (gateway).
2. Gateway proxies to the client app (`:8999`), which triggers OIDC login.
3. OIDC login/authorize endpoints are routed via gateway to the auth server (`:9000/auth`).
4. Login page, consent, and all OIDC redirects go through the gateway.
5. After login, user is redirected back to the client via the gateway.

### Required Auth Server Configuration

**`spring-auth-server-base/src/main/resources/application.properties`:**
```properties
spring.application.name=spring-boot-auth-server-base-0
server.port=9000
server.servlet.context-path=/auth
spring.security.oauth2.authorizationserver.issuer=http://localhost:8000/auth
server.forward-headers-strategy=native
```
- The **issuer** must match the gateway’s `/auth` endpoint.
- `server.forward-headers-strategy=native` ensures correct redirect URLs.

**Java Bean Example (if needed):**
```java
@Bean
public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
            .issuer("http://localhost:8000/auth")
            .build();
}
```

### Required Client App Configuration

**`spring-boot-client-base/src/main/resources/application.properties`:**
```properties
spring.security.oauth2.client.provider.client-oidc.issuer-uri=http://localhost:8000/auth
spring.security.oauth2.client.registration.client-oidc.client-id=client
spring.security.oauth2.client.registration.client-oidc.client-secret=secret
spring.security.oauth2.client.registration.client-oidc.scope=openid,profile
spring.security.oauth2.client.registration.client-oidc.redirect-uri=http://localhost:8000/client/login/oauth2/code/client-oidc
```
- Both the **issuer URI** and **redirect URI** point to the gateway.

### Example OIDC Flow

1. User accesses a protected route on the client via the gateway.
2. The client redirects to the OIDC authorization endpoint (proxied by the gateway).
3. The user logs in via the auth server (proxied by the gateway).
4. After authentication, the user is redirected back to the client (again, via the gateway).

---

## How It Works

- **Spring Cloud Gateway** acts as a reverse proxy, forwarding requests to backend microservices based on route predicates.
- **Dynamic Routing** is configured in `application.yml`—no controller code is needed for basic proxying.
- **OIDC Authentication** is routed through the gateway for security and consistency.
- **Observability** is enabled with Micrometer, Zipkin, and structured logging.
- **Extensibility**: You can add filters for authentication, rate limiting, circuit breaking, etc.

---

## Example Usage

Start your backend service on port 8080, then run the gateway:

```bash
./gradlew bootRun
```

Test the routing:

```bash
curl --location 'http://localhost:8070/api/v1/program/client/task/list'
```

To test OIDC authentication, visit:

```
http://localhost:8000/client
```

You should be redirected through the gateway to the login page, and after authentication, back to the client—all via the gateway.

---

---

### JWT Role Extraction and Mapping

By default, Spring Security expects roles to be in the `scope` or `authorities` claim and prefixed with `ROLE_`.  
If your JWT uses a custom claim (like `roles`) or does not use the `ROLE_` prefix, you must configure a converter.

**In this project:**
- The JWT is expected to have a claim named `roles`, e.g.:
  ```json
  {
    "sub": "user1",
    "roles": ["USER", "ADMIN"]
  }
  ```
- The gateway uses a custom `JwtGrantedAuthoritiesConverter` to extract roles from the `roles` claim and add the `ROLE_` prefix.

---

### Gateway Security Configuration

See [`GatewaySecurityConfig.java`](src/main/java/com/tanvir/gateway/GatewaySecurityConfig.java):

```java
@Bean
public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthoritiesClaimName("roles");
    authoritiesConverter.setAuthorityPrefix("ROLE_"); // Adds ROLE_ prefix to each role

    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt ->
        Flux.fromIterable(authoritiesConverter.convert(jwt))
    );

    http
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/auth/**", "/login/**", "/oauth2/**", "/logout", "/error").permitAll()
            .pathMatchers("/client/**").permitAll()
            .pathMatchers("/api/v1/program/client/task/list").hasRole("USER")
            .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .anyExchange().authenticated()
        )
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
        );
    return http.build();
}
```

**Key points:**
- The converter maps the `roles` claim to Spring Security authorities.
- `.hasRole("USER")` matches a JWT with `"roles": ["USER"]` (because the converter adds the `ROLE_` prefix).
- You can control which endpoints require which roles using `.hasRole("...")` or `.hasAuthority("...")`.

---

### Customizing Role Mapping

If you want to use the roles as-is (without the `ROLE_` prefix), set:
```java
authoritiesConverter.setAuthorityPrefix("");
```
And use `.hasAuthority("USER")` in your access rules.

---

### Example JWT

A valid JWT for a user with both roles:
```json
{
  "sub": "user1",
  "roles": ["USER", "ADMIN"],
  "exp": 1234567890
}
```

---

### Summary

- The gateway validates JWTs and enforces RBAC using roles from the token.
- Role extraction and mapping is fully customizable via the JWT converter.
- All RBAC logic is centralized at the gateway, keeping downstream services simple and secure.

---

---

## References

- [Spring Cloud Gateway Reference](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring Cloud Release Train Compatibility](https://spring.io/projects/spring-cloud#overview)
- [Spring Authorization Server Docs](https://docs.spring.io/spring-authorization-server/docs/current/reference/html/)
- [Spring Security OAuth2 Client Docs](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)