package ru.skillbox.social_network_bot.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class TokenService {
    private String token;

    private Long chatId;
}