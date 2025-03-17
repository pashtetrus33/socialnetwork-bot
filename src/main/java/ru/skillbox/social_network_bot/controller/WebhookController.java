package ru.skillbox.social_network_bot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.skillbox.social_network_bot.service.TelegramBotService;

@RestController
public class WebhookController {

    private final TelegramBotService telegramBotService;

    public WebhookController(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @PostMapping("/webhook")
    public void onUpdateReceived(@RequestBody Update update) {
        telegramBotService.onWebhookUpdateReceived(update);
    }
}