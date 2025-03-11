package ru.skillbox.social_network_bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.skillbox.social_network_bot.config.TelegramBotConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final TelegramBotConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, String>> getPosts() {
        try {
            return restTemplate.getForObject(config.getApiUrl(), List.class);
        } catch (Exception e) {
            log.error("Ошибка получения постов: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}