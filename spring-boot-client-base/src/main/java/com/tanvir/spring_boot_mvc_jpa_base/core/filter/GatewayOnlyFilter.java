package com.tanvir.spring_boot_mvc_jpa_base.core.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GatewayOnlyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String forwardedHost = req.getHeader("X-Forwarded-Host");
        if (!"localhost:8000".equals(forwardedHost)) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Direct access not allowed");
            return;
        }
        chain.doFilter(request, response);
    }
}