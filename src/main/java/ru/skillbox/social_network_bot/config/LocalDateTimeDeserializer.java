package ru.skillbox.social_network_bot.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();
        if (dateString.contains(".")) {
            // Дополняем миллисекунды до 6 знаков
            String[] parts = dateString.split("\\.");
            while (parts[1].length() < 6) {
                parts[1] += "0";
            }
            dateString = parts[0] + "." + parts[1];
        }
        return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
    }
}