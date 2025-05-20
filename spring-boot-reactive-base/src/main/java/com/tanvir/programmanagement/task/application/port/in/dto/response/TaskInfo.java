package com.tanvir.programmanagement.task.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskInfo {
    private String exercises;
    private String vitals;
    private String questionnaires;
}
