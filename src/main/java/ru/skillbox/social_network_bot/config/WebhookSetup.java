package ru.skillbox.social_network_bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.skillbox.social_network_bot.service.TelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookSetup implements CommandLineRunner {

    private final TelegramBot bot;
    private final TelegramBotConfig config;

    @Override
    public void run(String... args) {
        try {
            SetWebhook webhook = SetWebhook.builder().url(config.getWebhookUrl()).build();
            bot.setWebhook(webhook);
            log.info("Вебхук установлен на {}", config.getWebhookUrl());
        } catch (TelegramApiException e) {
            log.error("Ошибка установки вебхука: {}", e.getMessage());
        }
    }
}