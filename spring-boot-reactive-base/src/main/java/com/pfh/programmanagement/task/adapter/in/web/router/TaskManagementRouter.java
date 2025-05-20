package com.pfh.programmanagement.task.adapter.in.web.router;

import com.pfh.programmanagement.core.routes.RouteNames;
import com.pfh.programmanagement.task.adapter.in.web.handler.TaskManagementHandller;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class TaskManagementRouter {
    private final TaskManagementHandller handler;
    @Bean
    public RouterFunction<ServerResponse> taskManagementRouterConfig() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(RouteNames.PROGRAM_BASE_URL.concat(RouteNames.TASK).concat(RouteNames.DETAILS), handler::getTaskDetails)
                        .GET(RouteNames.PROGRAM_BASE_URL.concat(RouteNames.TASK).concat(RouteNames.LIST), handler::getTaskList)
                )
                .build();
    }
}
