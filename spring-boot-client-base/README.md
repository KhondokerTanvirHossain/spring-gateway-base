# spring-boot-mvc-jpa-base

This project is a base template for a Spring Boot application with MVC and JPA. It includes request and response logging using Logback and tracing with Micrometer and Zipkin.

## Features

* Spring Boot MVC
* JPA for database interactions
* Request and response logging
* Distributed tracing with Micrometer and Zipkin

## Setup

Prerequisites

* Java 17 or higher
* Spring Boot 3 or higher
* Zipkin server running locally (default endpoint: http://localhost:9411)

## Dependencies

Add the following dependencies to your build.gradle file:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-tracing-bridge-brave'
implementation 'io.zipkin.reporter2:zipkin-reporter-brave'
```

## Configuration

Add the following properties to your application.properties file:

```application.properties
management.tracing.enabled=true
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
```

## Logback Configuration

Add the following Logback pattern to your logback-spring.xml:

```xml
<Pattern>
    %d{yyyy-MM-dd HH:mm:ss.SSS} [ ${spring.application.name}, %X{spanId:-}, %X{traceId:-}, %X{Method}, %X{Uri} ] [Request-Trace-Id: %X{Request-Trace-Id}] [%t] %highlight(%-5level) %yellow(%class{0}) - %msg%n%throwable
</Pattern>
```

## Web Filter

The RequestCachingFilter class is used to log request and response details. Annotate it with @Component and @WebFilter:

```java
@Component
@RequiredArgsConstructor
@WebFilter(filterName = "RequestCachingFilter", urlPatterns = "/*")
public class RequestCachingFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
        CachedHttpServletRequest cachedRequest = new CachedHttpServletRequest(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        Collections.list(request.getHeaderNames()).forEach(headerName ->
            mutableRequest.putHeader(headerName, request.getHeader(headerName))
        );

        setRequestHeaders(mutableRequest);
        String requestBody = getRequestBody(mutableRequest);
        setMdcAttributeForLogBack(mutableRequest);
        logRequest(mutableRequest, requestBody);

        filterChain.doFilter(cachedRequest, wrappedResponse);

        String responseBody = getResponseBody(wrappedResponse);
        response.getHeaderNames().forEach(headerName ->
            wrappedResponse.setHeader(headerName, response.getHeader(headerName))
        );
        setResponseHeaders(mutableRequest, wrappedResponse);
        logResponse(mutableRequest, wrappedResponse, responseBody);
        wrappedResponse.copyBodyToResponse();
    }

    private void setRequestHeaders(MutableHttpServletRequest request) {
        // Implementation here
    }

    private void setResponseHeaders(MutableHttpServletRequest request, HttpServletResponse response) {
        // Implementation here
    }

    private String getRequestBody(MutableHttpServletRequest request) {
        // Implementation here
    }

    private void setMdcAttributeForLogBack(MutableHttpServletRequest request) {
        // Implementation here
    }

    private void logRequest(MutableHttpServletRequest request, String requestBody) {
        // Implementation here
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        // Implementation here
    }

    private void logResponse(MutableHttpServletRequest request, ContentCachingResponseWrapper response, String responseBody) {
        // Implementation here
    }
}
```

## Running the Application

1. Start the Zipkin server.
2. Build and run the Spring Boot application using your IDE or the command line:

```bash
./gradlew bootRun
```

3. The application will start and log request and response details with tracing information.


## Conclusion

This base template provides a starting point for building Spring Boot applications with MVC, JPA, request/response logging, and distributed tracing. Customize it further to fit your specific requirements.
