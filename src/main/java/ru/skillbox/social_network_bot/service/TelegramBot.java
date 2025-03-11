package ru.skillbox.social_network_bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.skillbox.social_network_bot.config.TelegramBotConfig;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramWebhookBot {

    private final TelegramBotConfig config;
    private final PostService postService;

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public String getBotPath() {
        return "/webhook";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if ("/start".equals(text)) {
                sendMessage(chatId, "Привет! Я бот, который показывает посты.");
            } else if ("/posts".equals(text)) {
                List<Map<String, String>> posts = postService.getPosts();
                if (posts.isEmpty()) {
                    sendMessage(chatId, "Нет новых постов.");
                } else {
                    for (Map<String, String> post : posts) {
                        sendMessage(chatId, "📢 " + post.get("title") + "\n\n" + post.get("content"));
                    }
                }
            }
        }
        return null;
    }

    public void sendMessage(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: {}", e.getMessage());
        }
    }
}