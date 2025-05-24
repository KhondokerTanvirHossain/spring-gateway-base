package com.tanvir.spring_boot_mvc_jpa_base.core.config.enums;

import lombok.Getter;

@Getter
public enum DateTimeFormatterPattern {
	DATE_TIME("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"),
	;
	private final String value;
	DateTimeFormatterPattern(String value){
		this.value = value;
	}
	

}
