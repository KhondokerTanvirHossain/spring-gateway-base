package com.tanvir.programmanagement.content.application.service;

import com.tanvir.programmanagement.content.application.port.in.dto.response.ContentResponseDto;
import com.tanvir.programmanagement.content.application.port.out.ContentWebClientPort;
import com.tanvir.programmanagement.core.ff4j.application.port.in.FF4jUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ContentServiceTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FF4jUseCase ff4jUseCase;

    @MockitoBean
    private ContentWebClientPort contentWebClientPort;

    @Test
    void shouldReturnContentListSuccessfullyWhenRequestWithSpecificContentType() {
        String clientId = "pfh";
        String contentTypeId = "exercise";

        ContentResponseDto mockResponse = ContentResponseDto.builder()
                .userMessage("Content Fetched Successfully.")
                .contents("Hello World. This is exercise List")
                .build();

        Mockito.when(ff4jUseCase.checkFeatureStatus("content"))
                .thenReturn(Mono.just("enabled"));

        Mockito.when(contentWebClientPort.getContentList(clientId, contentTypeId))
                .thenReturn(Mono.just(mockResponse));

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/content/{contentTypeId}/list", clientId, contentTypeId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userMessage").isEqualTo("Content Fetched Successfully.")
                .jsonPath("$.contents").isEqualTo("Hello World. This is exercise List");

        Mockito.verify(ff4jUseCase, Mockito.times(1)).checkFeatureStatus("content");
        Mockito.verify(contentWebClientPort, Mockito.times(1)).getContentList(clientId, contentTypeId);
    }


    @Test
    void shouldReturnForbiddenWhenFeatureIsDisabled() {
        String clientId = "pfh";
        String contentTypeId = "exercise";

        Mockito.when(ff4jUseCase.checkFeatureStatus("content"))
                .thenReturn(Mono.just("disabled"));

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/content/{contentTypeId}/list", clientId, contentTypeId)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Feature is currently unavailable!");

        Mockito.verify(contentWebClientPort, Mockito.times(0)).getContentList(clientId, contentTypeId);
    }
}