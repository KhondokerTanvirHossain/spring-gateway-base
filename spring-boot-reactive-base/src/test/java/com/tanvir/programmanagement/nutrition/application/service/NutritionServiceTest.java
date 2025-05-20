package com.tanvir.programmanagement.nutrition.application.service;

import com.pfh.programmanagement.core.ff4j.application.port.in.FF4jUseCase;
import com.pfh.programmanagement.nutrition.application.port.in.response.NutritionResponseDto;
import com.pfh.programmanagement.nutrition.application.port.out.NutritionWebClientPort;
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
class NutritionServiceTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private NutritionWebClientPort nutritionWebClientPort;

    @MockitoBean
    private FF4jUseCase ff4jUseCase;

    @Test
    void shouldReturnNutritionDataSuccessfully() {
        String clientId = "pfh";

        NutritionResponseDto mockResponse = NutritionResponseDto.builder()
                .userMessage("Nutrition Fetched Successfully.")
                .nutritionData("Hello World. This is nutrition data.")
                .build();

        Mockito.when(ff4jUseCase.checkFeatureStatus("nutrition"))
                .thenReturn(Mono.just("enabled"));

        Mockito.when(nutritionWebClientPort.getNutrition(clientId))
                .thenReturn(Mono.just(mockResponse));

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/nutrition", clientId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userMessage").isEqualTo("Nutrition Fetched Successfully.")
                .jsonPath("$.nutritionData").isEqualTo("Hello World. This is nutrition data.");

        Mockito.verify(ff4jUseCase, Mockito.times(1)).checkFeatureStatus("nutrition");
        Mockito.verify(nutritionWebClientPort, Mockito.times(1)).getNutrition(clientId);
    }

    @Test
    void shouldReturnForbiddenWhenFeatureIsDisabled() {
        String clientId = "pfh";

        Mockito.when(ff4jUseCase.checkFeatureStatus("nutrition"))
                .thenReturn(Mono.just("disabled"));

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/nutrition", clientId)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Feature is currently unavailable!");

        Mockito.verify(nutritionWebClientPort, Mockito.times(0)).getNutrition(clientId);
    }
}