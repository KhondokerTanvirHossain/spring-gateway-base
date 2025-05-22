package com.tanvir.spring_boot_mvc_jpa_base.core.filter.helpers.enums;

import lombok.Getter;

@Getter
public enum MDCKeys {
    TRACE_ID("Trace-ID"),
    METHOD("Method"),
    URI("Uri"),
    Request_Trace_Id("Request-Trace-Id"),
    ;

    private final String value;

    MDCKeys(String value) {
        this.value = value;
    }
}
