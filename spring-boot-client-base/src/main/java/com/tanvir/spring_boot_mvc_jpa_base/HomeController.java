package com.tanvir.spring_boot_mvc_jpa_base;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final WebClient webClient;

    public HomeController(@Qualifier("gatewayWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/")
    public String home(
            Model model,
            OAuth2AuthenticationToken authentication,
            @RegisteredOAuth2AuthorizedClient("client-oidc") OAuth2AuthorizedClient authorizedClient
    ) {
        Map<String, Object> userAttributes = authentication.getPrincipal().getAttributes();
        model.addAttribute("user", userAttributes);
        model.addAttribute("authorities", authentication.getAuthorities());
        var roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toList());
        model.addAttribute("roles", roles);

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String apiUrl = "http://localhost:8000/api/v1/program/client/task/list";

        try {
            Map taskData = webClient.get()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            model.addAttribute("taskData", taskData);
        } catch (WebClientResponseException.Forbidden ex) {
            model.addAttribute("taskError", "You do not have permission to view the task list (403 Forbidden).");
        } catch (WebClientResponseException ex) {
            model.addAttribute("taskError", "Failed to load task list: " + ex.getStatusCode());
        }

        return "home";
    }
}