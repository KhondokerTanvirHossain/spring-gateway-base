# Spring Cloud Gateway Base

A comprehensive Spring Cloud Gateway example repository featuring essential gateway capabilities for modern microservices architectures. This project demonstrates how to build a robust API Gateway with routing, security, observability, and extensibility in mind.

---

## Overview

This project is a template for building a production-ready API Gateway using Spring Cloud Gateway. It includes:

- Dynamic routing to microservices
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
```

- Requests to `http://localhost:8070/api/v1/program/client/task/list` are forwarded to `http://localhost:8080/api/v1/program/client/task/list`.

---

## How It Works

- **Spring Cloud Gateway** acts as a reverse proxy, forwarding requests to backend microservices based on route predicates.
- **Dynamic Routing** is configured in `application.yml`—no controller code is needed for basic proxying.
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

---

## Roadmap

- [x] Basic routing via Spring Cloud Gateway
- [ ] JWT authentication and role-based authorization
- [ ] Circuit breaker and fallback routes
- [ ] Request/response logging and distributed tracing
- [ ] Rate limiting and caching
- [ ] Feature flag management

---

## Technologies

- Java 21
- Spring Boot 3.4.2
- Spring Cloud Gateway 2024.0.1
- Micrometer Tracing & Zipkin
- Logback with Logstash encoder
- Lombok, ModelMapper

---

## License

MIT

---

## References

- [Spring Cloud Gateway Reference](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring Cloud Release Train Compatibility](https://spring.io/projects/spring-cloud#overview)