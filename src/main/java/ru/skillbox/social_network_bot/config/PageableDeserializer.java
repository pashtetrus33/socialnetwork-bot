package ru.skillbox.social_network_bot.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageableDeserializer extends JsonDeserializer<Pageable> {
    @Override
    public Pageable deserialize(JsonParser p, DeserializationContext ctxt) {
        return PageRequest.of(0, 10);
    }
}