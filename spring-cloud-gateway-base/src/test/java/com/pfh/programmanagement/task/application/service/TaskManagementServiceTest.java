package com.pfh.programmanagement.task.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TaskManagementServiceTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnTaskDetailsSuccessfully() {
        String clientId = "pfh";
        String taskId = "iku874ruh";

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/task/details/{taskId}", clientId, taskId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userMessage").isEqualTo("Task detail fetched successfully.")
                .jsonPath("$.data").isEqualTo("Daily Task Details");
    }


    @Test
    void shouldReturnTaskListSuccessfully() {
        String clientId = "pfh";

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/task/list", clientId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userMessage").isEqualTo("Daily task fetched successfully.")
                .jsonPath("$.tasks").isArray()
                .jsonPath("$.tasks[0].exercises").isEqualTo("Do some exercises")
                .jsonPath("$.tasks[0].vitals").isEqualTo("Take video vitals")
                .jsonPath("$.tasks[0].questionnaires").isEqualTo("Answer some questions")
                .jsonPath("$.tasks[1].exercises").isEqualTo("Do some exercises")
                .jsonPath("$.tasks[1].vitals").isEqualTo("Take video vitals")
                .jsonPath("$.tasks[1].questionnaires").isEqualTo("Answer some questions");
    }

}