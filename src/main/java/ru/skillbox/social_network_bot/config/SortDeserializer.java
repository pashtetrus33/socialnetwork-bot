package ru.skillbox.social_network_bot.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.data.domain.Sort;
import java.io.IOException;
import java.util.List;

public class SortDeserializer extends JsonDeserializer<Sort> {
    @Override
    public Sort deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<Sort.Order> orders = p.readValueAs(new TypeReference<>() {});
        return Sort.by(orders);
    }
}