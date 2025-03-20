package ru.skillbox.social_network_bot.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
public class TokenService {
    private Map<Long, String> tokens = new HashMap<>();
    private Long chatId;
}