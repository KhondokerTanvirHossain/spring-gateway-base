package com.tanvir.programmanagement.user.application.service;

import com.tanvir.programmanagement.user.adapter.out.api.dto.UserInfoClientResponseDto;
import com.tanvir.programmanagement.user.application.port.in.dto.UserInfoRequestDto;
import com.tanvir.programmanagement.user.application.port.out.UserInfoClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class UserInfoServiceTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private UserInfoClientPort userInfoClientPort;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnUserInfoSuccessfullyWhenRequestWithUserId() {
        String clientId = "pfh";
        UserInfoRequestDto requestDto = new UserInfoRequestDto("gioljojho");

        UserInfoClientResponseDto mockResponse = UserInfoClientResponseDto.builder()
                .userId("U003")
                .clientId("C001")
                .build();

        Mockito.when(userInfoClientPort.getUserInfo(requestDto))
                .thenReturn(Mono.just(mockResponse));

        webTestClient.post()
                .uri("/api/v1/program/{clientId}/user/info", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userMessage").isEqualTo("User info fetched successfully.")
                .jsonPath("$.data").isEqualTo("User Info");

        Mockito.verify(userInfoClientPort, Mockito.times(1)).getUserInfo(requestDto);
    }

}