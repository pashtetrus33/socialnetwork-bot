package ru.skillbox.social_network_bot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.skillbox.social_network_bot.service.TelegramBot;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class WebhookController {

    private final TelegramBot telegramBot;

    @PostMapping("/webhook")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }
}