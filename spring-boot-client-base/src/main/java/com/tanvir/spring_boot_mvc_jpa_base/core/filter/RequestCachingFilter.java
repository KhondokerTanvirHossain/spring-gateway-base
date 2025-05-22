package com.tanvir.spring_boot_mvc_jpa_base.core.filter;

import com.tanvir.spring_boot_mvc_jpa_base.core.filter.helpers.CachedHttpServletRequest;
import com.tanvir.spring_boot_mvc_jpa_base.core.filter.helpers.MutableHttpServletRequest;
import com.tanvir.spring_boot_mvc_jpa_base.core.filter.helpers.enums.HeaderNames;
import com.tanvir.spring_boot_mvc_jpa_base.core.filter.helpers.enums.MDCKeys;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@WebFilter(filterName = "RequestCachingFilter", urlPatterns = "/*")
public class RequestCachingFilter extends OncePerRequestFilter {

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestCachingFilter.class);
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
        request.putHeader(HeaderNames.REQUEST_RECEIVED_TIME_IN_MS.getValue(), String.valueOf(LocalDateTime.now()));
        if (Objects.isNull(request.getHeader(HeaderNames.TRACE_ID.getValue())) || request.getHeader(HeaderNames.TRACE_ID.getValue()).isEmpty()) {
            request.putHeader(HeaderNames.TRACE_ID.getValue(), tracer.currentSpan().context().traceId());
        }
    }

    private void setResponseHeaders(MutableHttpServletRequest request, HttpServletResponse response) {
        String requestReceivedTime = (String) request.getAttribute(HeaderNames.REQUEST_RECEIVED_TIME_IN_MS.getValue());
        if (requestReceivedTime != null) {
            long processingTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                - LocalDateTime.parse(requestReceivedTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"))
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            response.setHeader(HeaderNames.RESPONSE_PROCESSING_TIME_IN_MS.getValue(), String.valueOf(processingTime));
        }
        response.setHeader(HeaderNames.RESPONSE_SENT_TIME_IN_MS.getValue(), String.valueOf(LocalDateTime.now()));
        response.setHeader(HeaderNames.TRACE_ID.getValue(), Objects.requireNonNullElse(request.getHeader(HeaderNames.TRACE_ID.getValue()), ""));
    }

    private void setMdcAttributeForLogBack(HttpServletRequest request) {
        MDC.put(MDCKeys.METHOD.getValue(), Objects.requireNonNull(request.getMethod()));
        MDC.put(MDCKeys.URI.getValue(), request.getRequestURI());
        MDC.put(MDCKeys.TRACE_ID.getValue(), tracer.currentSpan().context().traceId());
        MDC.put(MDCKeys.Request_Trace_Id.getValue(), request.getHeader(HeaderNames.TRACE_ID.getValue()) != null ? request.getHeader(HeaderNames.TRACE_ID.getValue()) : tracer.currentSpan().context().traceId());
    }

    private void logRequest(MutableHttpServletRequest request, String requestBody) {
        if (request.getRequestURI().contains("actuator") || request.getRequestURI().contains("swagger")) {
            return;
        }
        LOGGER.info("""
                        Request Received From {}
                         Uri : {}
                         Method : {}
                         Headers : {}
                         Request Body : {}
                        """,
                request.getRemoteAddr(),
                request.getRequestURI(),
                request.getMethod(),
                getHeadersAsStringList(request),
                requestBody);
    }

    private void logResponse(HttpServletRequest request, ContentCachingResponseWrapper response, String responseBody) {
        LOGGER.info("""
                        Response Sending To {}
                         Uri : {}
                         Path : {}
                         Headers : {}
                         Response Status : {}
                         Content type : {}
                         Response Body : {}
                        """,
                request.getRemoteAddr(),
                request.getRequestURI(),
                request.getRequestURI(),
                getHeadersAsStringList(response),
                response.getStatus(),
                response.getContentType(),
                responseBody);
    }

    private List<String> getHeadersAsStringList(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .map(headerName -> headerName + ": " + Collections.list(request.getHeaders(headerName)))
                .collect(Collectors.toList());
    }

    private List<String> getHeadersAsStringList(HttpServletResponse response) {
        return response.getHeaderNames().stream()
                .map(headerName -> headerName + ": " + response.getHeaders(headerName))
                .collect(Collectors.toList());
    }

    private String getRequestBody(MutableHttpServletRequest request) {
        try {
            return StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Error reading the request body", e);
            return "";
        }
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length > 0) {
            return new String(buf, 0, buf.length, StandardCharsets.UTF_8);
        }
        return "";
    }

}
