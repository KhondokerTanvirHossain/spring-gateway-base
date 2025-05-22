# Spring Boot OIDC Gateway Architecture

This project demonstrates a secure, production-style setup for **Spring Boot microservices** using:
- **Spring Cloud Gateway** as an API gateway
- **Spring Authorization Server** as an OIDC provider
- **Spring Boot Client** as an OIDC client

All authentication flows, including login and logout, are routed **through the gateway**. This ensures a single entry point, consistent session/cookie handling, and a clean separation of concerns.

---

## Architecture Overview

```
[Browser]
   |
   v
[Gateway:8000] <----> [Client:8999]
   |
   v
[Auth Server:9000]
```

- **Gateway (`:8000`)**: All traffic (including OIDC) goes through here.
- **Client (`:8999`)**: OIDC client, protected routes.
- **Auth Server (`:9000`)**: OIDC provider, `/auth` context path.

---

## 1. Spring Cloud Gateway

**File:** `spring-cloud-gateway-base/src/main/resources/application.yml`

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: client
          uri: http://localhost:8999
          predicates:
            - Path=/client/**
          filters:
            - StripPrefix=1
        - id: auth-server
          uri: http://localhost:9000
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=0
```

- `/client/**` → client app (strip `/client`)
- `/auth/**` → auth server (keep `/auth`)

**Start the gateway:**
```bash
cd spring-cloud-gateway-base
./gradlew bootRun
```

---

## 2. Spring Authorization Server

**File:** `spring-auth-server-base/src/main/resources/application.properties`

```properties
spring.application.name=spring-boot-auth-server-base-0
server.port=9000
server.servlet.context-path=/auth
spring.security.oauth2.authorizationserver.issuer=http://localhost:8000/auth
server.forward-headers-strategy=native
```

- **Issuer** is the gateway’s `/auth` endpoint.
- **Forward headers** ensures redirects use the gateway’s host/port.

**File:** `spring-auth-server-base/src/main/java/com/tanvir/AuthServerConfig.java`

```java
@Bean
public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
            .issuer("http://localhost:8000/auth")
            .build();
}
@Bean
@Order(1)
public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
    OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
    authorizationServerConfigurer.oidc(Customizer.withDefaults()); // Enable OIDC

    http
            .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
            .apply(authorizationServerConfigurer);

    return http.formLogin(Customizer.withDefaults()).build();
}

@Bean
@Order(2)
public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults()) // ✅ Do NOT set loginPage("/auth/login")
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("http://localhost:8000/client/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            );

    return http.build();
}
```

**Registered OIDC Client:**
```java
@Bean
public RegisteredClientRepository registeredClientRepository() {
    RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("client")
            .clientSecret("{noop}secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost:8000/client/login/oauth2/code/client-oidc")
            .scope(OidcScopes.OPENID)
            .scope("profile")
            .build();

    return new InMemoryRegisteredClientRepository(registeredClient);
}
```

**Start the auth server:**
```bash
cd spring-auth-server-base
./gradlew bootRun
```

---

## 3. Spring Boot OIDC Client

**File:** `spring-boot-client-base/src/main/resources/application.properties`

```properties
spring.application.name=spring-boot-client-base-0
server.port=8999

spring.security.oauth2.client.provider.client-oidc.issuer-uri=http://localhost:8000/auth
spring.security.oauth2.client.registration.client-oidc.client-id=client
spring.security.oauth2.client.registration.client-oidc.client-secret=secret
spring.security.oauth2.client.registration.client-oidc.scope=openid,profile
spring.security.oauth2.client.registration.client-oidc.redirect-uri=http://localhost:8000/client/login/oauth2/code/client-oidc

logging.level.org.springframework.security.oauth2.client=TRACE
```

- **Issuer URI** and **redirect URI** both point to the gateway.

**Start the client:**
```bash
cd spring-boot-client-base
./gradlew bootRun
```

---

## 4. How the OIDC Flow Works

1. **User visits** `http://localhost:8000/client`
2. Gateway proxies to client (`:8999`), which triggers OIDC login.
3. OIDC login/authorize endpoints are routed via gateway to auth server (`:9000/auth`).
4. Login page, consent, and all OIDC redirects go through the gateway.
5. After login, user is redirected back to client via gateway.

---

## 5. Common Issues & Solutions

- **Login page not via gateway:**  
  Ensure `server.forward-headers-strategy=native` is set in the auth server.
- **OIDC issuer mismatch:**  
  The issuer in both the auth server config and OIDC client config must be `http://localhost:8000/auth`.
- **404 favicon.ico:**  
  Add a `favicon.ico` to `spring-boot-client-base/src/main/resources/static/` to avoid this warning.

---

## 6. Example: Add a Home Page to Client

**File:** `spring-boot-client-base/src/main/resources/templates/index.html`

```html
<!DOCTYPE html>
<html>
<head>
    <title>Welcome OIDC User</title>
</head>
<body>
<h1>Welcome OIDC User</h1>
<ul>
    <li><strong>Username:</strong> <span th:text="${#authentication.name}"></span></li>
    <li><strong>Issuer:</strong> <span th:text="${issuer}"></span></li>
    <li><strong>Audience:</strong> <span th:text="${audience}"></span></li>
    <li><strong>Authentication Time:</strong> <span th:text="${authTime}"></span></li>
</ul>
<form action="/client/logout" method="post">
    <input type="hidden" name="_csrf" th:value="${_csrf.token}"/>
    <button type="submit">Logout</button>
</form>
</body>
</html>
```

---

## 7. Running the Full Stack

1. **Start all services** (gateway, auth server, client).
2. Visit [http://localhost:8000/client](http://localhost:8000/client)
3. Login with:
   - Username: `testuser`
   - Password: `password`
4. You will see the welcome page after login.

---

## 8. Security Notes

- All cookies and sessions are managed via the gateway domain/port.
- All OIDC endpoints are protected and routed through the gateway.
- No direct access to the auth server from the browser.

---

## 9. Troubleshooting

- **Login page not loading via gateway:**  
  Double-check `server.forward-headers-strategy=native` and gateway route for `/auth/**`.
- **OIDC errors:**  
  Ensure all URIs in configs use the gateway’s port and context.
- **404 favicon.ico:**  
  Add a favicon as described above.

---

## 10. Useful Commands

**Build all projects:**
```bash
./gradlew build
```

**Run with Gradle:**
```bash
./gradlew bootRun
```

---

## 11. References

- [Spring Authorization Server Docs](https://docs.spring.io/spring-authorization-server/docs/current/reference/html/)
- [Spring Cloud Gateway Docs](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring Security OAuth2 Client Docs](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)

---

## 12. Contact

For questions or issues, please open an issue on this repository.

---

**This setup provides a robust, production-style OIDC gateway architecture for Spring Boot microservices.**