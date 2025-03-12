package ru.skillbox.social_network_bot.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class WebhookRegistrar {

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.webhook-url}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    public WebhookRegistrar(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void setWebhook() {
        // URL для установки вебхука
        String url = "https://api.telegram.org/bot" + botToken + "/setWebhook?url=" + webhookUrl;
        restTemplate.postForObject(url, null, String.class);  // Отправляем запрос на установку вебхука
    }
}