package ru.skillbox.social_network_bot.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter ISO_MICROS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private static final DateTimeFormatter ISO_NO_MICROS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();

        try {
            if (dateString.contains(".")) {
                String[] parts = dateString.split("\\.");
                while (parts[1].length() < 6) {
                    parts[1] += "0";
                }
                dateString = parts[0] + "." + parts[1];
                return LocalDateTime.parse(dateString, ISO_MICROS);
            } else {
                return LocalDateTime.parse(dateString, ISO_NO_MICROS);
            }
        } catch (DateTimeParseException e) {
            throw new IOException("Failed to parse date: " + dateString, e);
        }
    }
}