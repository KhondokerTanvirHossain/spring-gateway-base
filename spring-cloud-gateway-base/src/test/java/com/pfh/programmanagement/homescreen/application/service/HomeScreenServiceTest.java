package com.tanvir.programmanagement.homescreen.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class HomeScreenServiceTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnHomepageDataSuccessfullyWhenRequested() {

        String clientId = "pfh";

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/homepage", clientId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userMessage").isEqualTo("Welcome to the Home Screen")
                .jsonPath("$.dailyTask").isEqualTo("This is the daily task")
                .jsonPath("$.contents.quickExercises").isEqualTo("This is the quick exercises")
                .jsonPath("$.contents.meditationPractices").isEqualTo("This is the meditation practices")
                .jsonPath("$.contents.learningContent").isEqualTo("This is the learning content");
    }

}