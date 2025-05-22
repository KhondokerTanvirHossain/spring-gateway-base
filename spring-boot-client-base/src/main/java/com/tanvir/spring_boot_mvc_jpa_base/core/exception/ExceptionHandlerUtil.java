package com.tanvir.spring_boot_mvc_jpa_base.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionHandlerUtil extends Exception {
    public HttpStatusCode code;
    public String message;
}
