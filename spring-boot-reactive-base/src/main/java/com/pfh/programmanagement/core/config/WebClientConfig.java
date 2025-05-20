package com.pfh.programmanagement.core.config;

import com.pfh.programmanagement.core.enums.DateTimeFormatterPattern;
import com.pfh.programmanagement.core.filters.HeaderNames;
import com.pfh.programmanagement.core.util.TracerUtil;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Configuration
@Slf4j
public class WebClientConfig {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateTimeFormatterPattern.DATE_TIME.getValue());
    private final HttpClient httpClient;
    private final Tracer tracer;


    @Value("${external.base-url}")
    private String EXTERNAL_BASE_URL;


    public WebClientConfig(@Qualifier("reactiveHttpClientWithTimeout") HttpClient httpClient, Tracer tracer) {
        this.httpClient = httpClient;
        this.tracer = tracer;
    }


    @Bean
    public WebClient questionnaireServiceWebClient() {
        return getWebClient(EXTERNAL_BASE_URL, httpClient);
    }

    private WebClient getWebClient(String baseUrl, HttpClient httpClient) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((ClientRequest request, ExchangeFunction next) -> {
                    ClientRequest updatedRequest = setRequestHeaders(request);
                    logRequest(updatedRequest);
                    return next.exchange(updatedRequest)
                            .doOnNext((ClientResponse response) -> {
                                setResponseHeaders(updatedRequest, response);
                                logResponse(response, baseUrl);
                            });
                })
                .build();
    }


    private ClientRequest setRequestHeaders(ClientRequest request) {
        String traceId;
        if (tracer.currentSpan() != null) {
            traceId = Objects.requireNonNull(tracer.currentSpan()).context().traceId();
        } else {
            traceId = "default-trace-id";
        }
        if (Optional.ofNullable(request.headers().get(HeaderNames.TRACE_ID.getValue())).isPresent()
                && !Objects.requireNonNull(request.headers().get(HeaderNames.TRACE_ID.getValue()))
                .stream()
                .allMatch(String::isEmpty))
            traceId = Objects.requireNonNull(request.headers().get(HeaderNames.TRACE_ID.getValue()))
                    .stream()
                    .findFirst()
                    .orElse(traceId);
        return ClientRequest.from(request)
                .header(HeaderNames.REQUEST_SENT_TIME_IN_MS.getValue(), LocalDateTime.now().format(formatter))
                .header(HeaderNames.TRACE_ID.getValue(), traceId)
                .build();
    }

    private void setResponseHeaders(ClientRequest request, ClientResponse response) {
        response.mutate().header(HeaderNames.RESPONSE_RECEIVED_TIME_IN_MS.getValue(), LocalDateTime.now().format(formatter));
        if (!Objects.requireNonNull(request.headers().get(HeaderNames.REQUEST_SENT_TIME_IN_MS.getValue()))
                .stream()
                .allMatch(String::isEmpty)) {
            String s = Objects.requireNonNull(request.headers().get(HeaderNames.REQUEST_SENT_TIME_IN_MS.getValue()))
                    .stream()
                    .findAny().orElse(LocalDateTime.now().format(formatter));
            response.mutate().header(HeaderNames.RESPONSE_TRANSMISSION_TIME_IN_MS.getValue(),
                    String.valueOf(
                            LocalDateTime.now()
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                    - LocalDateTime
                                    .parse(s, formatter)
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                    ));
        }
    }

    private void logRequest(ClientRequest request) {
        try {
            log.info("""
                            Request Sending From {}
                             Uri : {}
                             Method : {}
                             Headers : {}
                             Content type : {}
                             Acceptable Media Type {}
                            """,
                    InetAddress.getLocalHost().getHostAddress(),
                    request.url(),
                    request.method(),
                    request.headers(),
                    request.headers().getContentType(),
                    request.headers().getAccept());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void logResponse(ClientResponse response, String baseUrl) {
        log.info("""
                        Response Receiving from {}
                         Headers : {}
                         Response Status : {}
                         Content type : {}
                        """,
                baseUrl,
                response.headers().asHttpHeaders(),
                response.statusCode(),
                response.headers().contentType()
        );
        log.info("Response receiving time from {} is {} ms", baseUrl,
                Objects.requireNonNull(response.headers()
                        .header(HeaderNames.RESPONSE_TRANSMISSION_TIME_IN_MS.getValue())
                        .stream()
                        .findFirst()
                        .orElse("0")
                ));
    }

}

