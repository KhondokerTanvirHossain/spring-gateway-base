package com.tanvir.spring_boot_mvc_jpa_base.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebClientExceptionHandlerUtil extends RuntimeException {
    public HttpStatusCode code;
    public String message;
    public Map<String, String> body;
}
