# spring-boot-client-base

This project is a base template for a **Spring Boot MVC** application with OIDC authentication, Thymeleaf templating, JPA, request/response logging, and distributed tracing. It is designed to work as an OIDC client behind a Spring Cloud Gateway, authenticating users via a Spring Authorization Server.

---

## Features

- **Spring Boot MVC** with Thymeleaf templates
- **OIDC Authentication** (OpenID Connect) via Spring Security
- **Works behind a Gateway**: All authentication flows go through Spring Cloud Gateway
- **JPA** for database interactions
- **Request and response logging** with Logback (plain and JSON)
- **Distributed tracing** with Micrometer and Zipkin
- **Custom error handling**
- **Sample home page** showing authenticated user info

---

## Architecture

```
[Browser]
   |
   v
[Gateway:8000] <----> [Client:8999]
   |
   v
[Auth Server:9000]
```
- All requests (including login/logout) are routed through the gateway (`:8000`).
- The client app (`:8999`) never exposes its own login endpoints directly.

---

## Prerequisites

- Java 17 or higher
- Spring Boot 3 or higher
- Spring Cloud Gateway running on port 8000
- Spring Authorization Server running on port 9000 (`/auth` context path)
- Zipkin server running locally (default endpoint: http://localhost:9411)

---

## OIDC Authentication Flow

1. **User visits** `http://localhost:8000/client` (via gateway).
2. If not authenticated, the client app redirects to the OIDC login flow.
3. The gateway proxies OIDC requests to the auth server (`:9000/auth`).
4. User logs in via the auth server (proxied by the gateway).
5. After login, user is redirected back to the client (again, via the gateway).
6. The home page displays user info from the OIDC ID token.

---

## Configuration

### `application.properties`

```properties
spring.application.name=spring-boot-client-base-0
server.port=8999

# OIDC Client Configuration (all URLs via gateway)
spring.security.oauth2.client.registration.client-oidc.client-id=client
spring.security.oauth2.client.registration.client-oidc.client-secret=secret
spring.security.oauth2.client.registration.client-oidc.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.client-oidc.redirect-uri=http://localhost:8000/client/login/oauth2/code/client-oidc
spring.security.oauth2.client.registration.client-oidc.scope=openid,profile

spring.security.oauth2.client.provider.client-oidc.issuer-uri=http://localhost:8000/auth

# Tracing and Logging
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
management.tracing.enabled=true
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans

LOG_DIR=/tmp/logs
MAX_FILE_SIZE=15MB
MAX_HISTORY=20
LOG_LEVEL_ROOT=INFO

logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.oauth2.client=TRACE
```

---

## Thymeleaf Home Page

**File:** `src/main/resources/templates/home.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Welcome</title>
</head>
<body>
<h1>Welcome OIDC User</h1>

<ul>
    <li><strong>Username:</strong> <span th:text="${user['sub']}"></span></li>
    <li><strong>Issuer:</strong> <span th:text="${user['iss']}"></span></li>
    <li><strong>Audience:</strong> <span th:text="${user['aud']}"></span></li>
    <li><strong>Authentication Time:</strong> <span th:text="${user['auth_time']}"></span></li>
</ul>

<!-- Logout via Gateway -->
<form th:action="@{http://localhost:8000/client/logout}" method="post">
    <button type="submit">Logout</button>
</form>
</body>
</html>
```

---

## Home Controller

**File:** `src/main/java/com/tanvir/spring_boot_mvc_jpa_base/HomeController.java`

```java
package com.tanvir.spring_boot_mvc_jpa_base;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, OAuth2AuthenticationToken authentication) {
        Map<String, Object> userAttributes = authentication.getPrincipal().getAttributes();
        model.addAttribute("user", userAttributes);
        return "home";
    }
}
```

---

## Logging and Tracing

- **Logback** is configured for both plain and JSON logs.
- **Micrometer** and **Zipkin** are enabled for distributed tracing.
- Request/response logging is handled by a custom filter (`RequestCachingFilter`).

---

## Running the Application

1. **Start the Gateway** (`:8000`), Auth Server (`:9000`), and Zipkin.
2. Build and run the client app:
    ```bash
    ./gradlew bootRun
    ```
3. Visit [http://localhost:8000/client](http://localhost:8000/client) in your browser.
4. Log in with your OIDC credentials (as configured in the auth server).
5. You will see the home page with your OIDC user info.

---

## Useful Endpoints

- `/` : Home page (shows OIDC user info)
- `/logout` : Logs out the user (via gateway)
- `/actuator/*` : Spring Boot Actuator endpoints

---

## Customization

- Add more Thymeleaf templates for additional pages.
- Extend the JPA entities and repositories for your domain.
- Customize logging and error handling as needed.

---

## References

- [Spring Security OAuth2 Client Docs](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Spring Authorization Server Docs](https://docs.spring.io/spring-authorization-server/docs/current/reference/html/)
- [Spring Cloud Gateway Docs](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Thymeleaf Docs](https://www.thymeleaf.org/documentation.html)

---

## Conclusion

This template provides a solid foundation for building secure, observable, and maintainable Spring Boot MVC applications with OIDC authentication, Thymeleaf, and modern logging/tracing. All authentication flows are routed through the gateway for maximum security and flexibility.