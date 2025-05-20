package com.pfh.programmanagement.baselinehealthassessment.application.service;

import com.pfh.programmanagement.baselinehealthassessment.adapter.out.web.dto.QuestionnaireResponse;
import com.pfh.programmanagement.baselinehealthassessment.application.port.in.dto.response.BaselineHealthAssessmentResponseDto;
import com.pfh.programmanagement.baselinehealthassessment.application.port.out.QuestionnaireWebclientPort;
import com.pfh.programmanagement.core.ff4j.application.port.in.FF4jUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BaselineHealthAssessmentServiceTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private QuestionnaireWebclientPort questionnaireWebclientPort;

    @MockitoBean
    private FF4jUseCase ff4jUseCase;

    @Test
    void shouldReturnBaselineHealthAssessmentQuestionsSuccessfullyWhenRequestWithSpecificQuestionnaireId() {
        String questionnaireId = "baseline";

        QuestionnaireResponse mockResponse = QuestionnaireResponse.builder()
                .userMessage("Baseline Questionnaire Fetched successfully.")
                .build();

        Mockito.when(ff4jUseCase.checkFeatureStatus("baseline-assessment"))
                .thenReturn(Mono.just("enabled"));

        Mockito.when(questionnaireWebclientPort.getQuestions(questionnaireId))
                .thenReturn(Mono.just(mockResponse));

        BaselineHealthAssessmentResponseDto responseBody = webTestClient.get()
                .uri("/api/v1/program/{clientId}/questionnaire/{questionnaireId}/questions", "pfh", questionnaireId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BaselineHealthAssessmentResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(responseBody);
        assertInstanceOf(String.class, responseBody.getUserMessage());
        assertEquals("Baseline Questionnaire Fetched successfully.", responseBody.getUserMessage());

        Mockito.verify(ff4jUseCase, Mockito.times(1)).checkFeatureStatus("baseline-assessment");
        Mockito.verify(questionnaireWebclientPort, Mockito.times(1)).getQuestions(questionnaireId);
    }


    @Test
    void shouldReturnForbiddenWhenFeatureIsDisabled() {
        String questionnaireId = "baseline";

        Mockito.when(ff4jUseCase.checkFeatureStatus("baseline-assessment"))
                .thenReturn(Mono.just("disabled"));

        webTestClient.get()
                .uri("/api/v1/program/{clientId}/questionnaire/{questionnaireId}/questions", "pfh", questionnaireId)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Feature is currently unavailable!");

        Mockito.verify(questionnaireWebclientPort, Mockito.times(0)).getQuestions(questionnaireId);
    }

}