package ru.skillbox.social_network_bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramBotService extends TelegramWebhookBot {

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.bot-username}")
    private String botUsername;

    @Override
    public String getBotUsername() {
        return botUsername;  // Имя вашего бота
    }

    @Override
    public String getBotToken() {
        return botToken;  // Токен вашего бота
    }

    @Override
    public String getBotPath() {
        return "/webhook";  // Путь для вебхука
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();  // Получаем текст сообщения
            Long chatId = update.getMessage().getChatId();  // Получаем ID чата

            // Создаем объект SendMessage для эхо-ответа
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Echo: " + messageText);  // Отправляем обратно тот же текст

            try {
                execute(message);  // Отправляем ответ пользователю
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}