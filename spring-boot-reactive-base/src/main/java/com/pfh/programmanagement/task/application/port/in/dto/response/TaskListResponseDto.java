package com.pfh.programmanagement.task.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskListResponseDto {
    private String userMessage;
    private List<TaskInfo> tasks;
}
