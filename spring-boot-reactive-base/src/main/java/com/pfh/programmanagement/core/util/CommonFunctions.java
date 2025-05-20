package com.pfh.programmanagement.core.util;

import com.google.gson.*;
import com.pfh.programmanagement.core.enums.DateTimeFormatterPattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
@Slf4j
public class CommonFunctions {
	public String buildGsonBuilder(Object object) {
		return buildGson(object).toJson(object);
	}

	public Gson buildGson(Object object) {
		DateTimeFormatter formater = DateTimeFormatter.ofPattern(DateTimeFormatterPattern.DATE_TIME.getValue());
		return new GsonBuilder()
				.registerTypeAdapter(LocalDateTime.class,
						(JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), formater))
				.registerTypeAdapter(LocalDateTime.class,
						(JsonSerializer<LocalDateTime>) (localDateTime, type, jsonSerializationContext) ->
								new JsonPrimitive(localDateTime.format(formater)))
				.registerTypeAdapter(LocalDate.class,
						(JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(),
								DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.registerTypeAdapter(LocalDate.class,
						(JsonSerializer<LocalDate>) (localDateTime, type, jsonSerializationContext) ->
								new JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
				.setPrettyPrinting().create();
	}

}
