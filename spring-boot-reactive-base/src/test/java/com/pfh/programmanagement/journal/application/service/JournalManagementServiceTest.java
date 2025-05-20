package com.pfh.programmanagement.journal.application.service;

import com.pfh.programmanagement.journal.application.port.in.dto.request.JournalEntryRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class JournalManagementServiceTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldSaveJournalEntrySuccessfullyWhenRequestedWithContentToSave() {
        String clientId = "pfh";
        JournalEntryRequestDto requestDto = new JournalEntryRequestDto("This is journal content to be saved");

        webTestClient.post()
                .uri("/api/v1/program/{clientId}/journal/entry", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userMessage").isEqualTo("Journal entry saved successfully.");
    }
}