package com.tanvir.programmanagement.welcome.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class WelcomeScreenIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnWelcomeScreenSuccessfully() {
        String clientId = "pfh";

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/welcome", clientId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userMessage").isEqualTo("Welcome Screen Fetched successfully.")
                .jsonPath("$.welcomeNote").isEqualTo("Hello World. This is welcome note.");
    }
}
