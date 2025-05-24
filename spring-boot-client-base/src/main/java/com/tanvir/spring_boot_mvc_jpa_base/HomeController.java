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

import java.util.Map;

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

        // Get access token
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String apiUrl = "http://localhost:8000/api/v1/program/client/task/list";

        Map taskData = webClient.get()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        model.addAttribute("taskData", taskData);

        return "home";
    }
}