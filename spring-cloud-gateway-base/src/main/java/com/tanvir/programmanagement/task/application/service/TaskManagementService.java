package com.pfh.programmanagement.task.application.service;

import com.pfh.programmanagement.task.application.port.in.TaskManagementUseCase;
import com.pfh.programmanagement.task.application.port.in.dto.response.TaskDetailResponseDto;
import com.pfh.programmanagement.task.application.port.in.dto.response.TaskInfo;
import com.pfh.programmanagement.task.application.port.in.dto.response.TaskListResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagementService implements TaskManagementUseCase {

    @Override
    public Mono<TaskDetailResponseDto> getTaskDetailById(String taskId) {

        return Mono.just(TaskDetailResponseDto.builder()
            .userMessage("Task detail fetched successfully.")
            .data("Daily Task Details")
            .build());
    }

    @Override
    public Mono<TaskListResponseDto> getTaskList() {
        return Flux.just(TaskInfo.builder()
                .exercises("Do some exercises")
                .questionnaires("Answer some questions")
                .vitals("Take video vitals")
                .build(), TaskInfo.builder()
                .vitals("Take video vitals")
                .exercises("Do some exercises")
                .questionnaires("Answer some questions")
                .build())
            .collectList()
            .map(taskInfos -> TaskListResponseDto.builder()
                .userMessage("Daily task fetched successfully.")
                .tasks(taskInfos)
                .build());
    }
}
