package ru.skillbox.social_network_bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "telegram")
public class TelegramBotConfig {
    private String botToken;
    private String botUsername;
    private String webhookUrl;
    private String apiUrl;
}