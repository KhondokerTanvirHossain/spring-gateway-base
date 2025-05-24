package com.tanvir.filter.helpers.enums;

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
