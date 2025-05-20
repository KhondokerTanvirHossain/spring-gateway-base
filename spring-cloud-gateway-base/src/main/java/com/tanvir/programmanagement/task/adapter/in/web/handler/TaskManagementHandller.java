package com.pfh.programmanagement.task.adapter.in.web.handler;

import com.pfh.programmanagement.core.util.exception.ErrorHandler;
import com.pfh.programmanagement.core.util.exception.ExceptionHandlerUtil;
import com.pfh.programmanagement.task.application.port.in.TaskManagementUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class TaskManagementHandller {

    private final TaskManagementUseCase taskManagementUseCase;

    public Mono<ServerResponse> getTaskDetails(ServerRequest serverRequest) {
        String clientId = serverRequest.pathVariable("clientId");
        String taskId  = serverRequest.pathVariable("taskId");
        return taskManagementUseCase.getTaskDetailById(taskId)
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getTaskList(ServerRequest serverRequest) {
        String clientId = serverRequest.pathVariable("clientId");
        return taskManagementUseCase.getTaskList()
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }
}
