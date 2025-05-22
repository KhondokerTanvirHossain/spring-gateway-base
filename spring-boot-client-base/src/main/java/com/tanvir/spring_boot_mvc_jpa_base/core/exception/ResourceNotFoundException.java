package com.tanvir.spring_boot_mvc_jpa_base.core.exception;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceNotFoundException {
    public HttpStatusCode code;
    public String message;
}
