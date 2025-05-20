package com.tanvir.programmanagement.myhealth.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MyHealthServiceTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnMyHealthDataSuccessfullyWhenRequested() {
        String clientId = "pfh";

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/my-health", clientId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userMessage").isEqualTo("My Health Fetched successfully.")
                .jsonPath("$.healthData").isEqualTo("Hello World. This is my health data.");
    }
}