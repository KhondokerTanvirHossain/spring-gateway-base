//package com.pfh.programmanagement.welcome.application.service;
//
//import com.pfh.programmanagement.core.util.exception.ExceptionHandlerUtil;
//import com.pfh.programmanagement.welcome.application.port.in.dto.response.WelcomeScreenResponseDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpStatus;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class WelcomeScreenServiceTest {
//
//    private WelcomeScreenService welcomeScreenService;
//
//    @BeforeEach
//    void setUp() {
//        welcomeScreenService = new WelcomeScreenService();
//    }
//
//    @Test
//    void getWelcomeScreen_ShouldReturnError_WhenClientIdIsNull() {
//        Mono<WelcomeScreenResponseDto> result = welcomeScreenService.getWelcomeScreen(null);
//
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof ExceptionHandlerUtil &&
//                                ((ExceptionHandlerUtil) throwable).getCode() == HttpStatus.BAD_REQUEST &&
//                                throwable.getMessage().equals("Client Id not found!")
//                )
//                .verify();
//    }
//
//    @Test
//    void getWelcomeScreen_ShouldReturnError_WhenClientIdIsEmpty() {
//        Mono<WelcomeScreenResponseDto> result = welcomeScreenService.getWelcomeScreen("");
//
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof ExceptionHandlerUtil &&
//                                ((ExceptionHandlerUtil) throwable).getCode() == HttpStatus.BAD_REQUEST &&
//                                throwable.getMessage().equals("Client Id not found!")
//                )
//                .verify();
//    }
//
//    @Test
//    void getWelcomeScreen_ShouldReturnError_WhenClientIdIsInvalid() {
//        Mono<WelcomeScreenResponseDto> result = welcomeScreenService.getWelcomeScreen("invalid");
//
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof ExceptionHandlerUtil &&
//                                ((ExceptionHandlerUtil) throwable).getCode() == HttpStatus.BAD_REQUEST &&
//                                throwable.getMessage().equals("Client not found!")
//                )
//                .verify();
//    }
//
//    @Test
//    void getWelcomeScreen_ShouldReturnSuccess_WhenClientIdIsValid() {
//        Mono<WelcomeScreenResponseDto> result = welcomeScreenService.getWelcomeScreen("pfh");
//
//        StepVerifier.create(result)
//                .expectNextMatches(response ->
//                        response.getUserMessage().equals("Welcome Screen Fetched successfully.") &&
//                                response.getWelcomeNote().equals("Hello World. This is welcome screen.")
//                )
//                .verifyComplete();
//    }
//}