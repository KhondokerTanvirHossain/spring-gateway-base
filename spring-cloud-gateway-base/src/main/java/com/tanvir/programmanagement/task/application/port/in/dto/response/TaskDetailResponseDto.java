package com.pfh.programmanagement.task.application.port.in.dto.response;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailResponseDto {
    private String userMessage;
    private String data;
}
