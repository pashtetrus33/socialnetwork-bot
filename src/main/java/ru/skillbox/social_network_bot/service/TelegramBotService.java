package ru.skillbox.social_network_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.skillbox.social_network_bot.client.AuthServiceClient;
import ru.skillbox.social_network_bot.dto.AuthenticateRq;
import ru.skillbox.social_network_bot.dto.TokenResponse;
import ru.skillbox.social_network_bot.dto.UserSession;
import ru.skillbox.social_network_bot.dto.UserState;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TelegramBotService extends TelegramWebhookBot {

    private final AuthServiceClient authServiceClient;

    private final Map<Long, UserSession> userSessions = new HashMap<>();
    private final String botUsername;

    public TelegramBotService(@Value("${telegram.bot-token}") String botToken, AuthServiceClient authServiceClient,
                              @Value("${telegram.bot-username}") String botUsername) {
        super(botToken);
        this.authServiceClient = authServiceClient;
        this.botUsername = botUsername;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotPath() {
        return "/webhook";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            UserSession userSession = userSessions.getOrDefault(chatId, new UserSession(chatId));
            userSession.setChatId(chatId);

            switch (userSession.getState()) {
                case DEFAULT:
                    if (text.equals("/start")) {
                        userSession.setState(UserState.AWAITING_LOGIN);
                        sendMessage(chatId, """
                                Пожалуйста, авторизуйтесь, введя логин и пароль.
                                Введите ваш логин.
                                """);
                    }
                    break;

                case AWAITING_LOGIN:
                    userSession.setLogin(text);
                    userSession.setState(UserState.AWAITING_PASSWORD);
                    sendMessage(chatId, "Теперь введите ваш пароль.");
                    break;

                case AWAITING_PASSWORD:
                    userSession.setPassword(text);
                    String login = userSession.getLogin();
                    String password = userSession.getPassword();

                    if (authenticateUser(login, password)) {
                        sendMessage(chatId, "Авторизация успешна!");
                    } else {
                        sendMessage(chatId, "Неверный логин или пароль. Попробуйте еще раз.");
                        userSession.setState(UserState.DEFAULT);
                    }
                    break;
            }

            userSessions.put(chatId, userSession);
        }
        return null;
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage());
        }
    }

    private boolean authenticateUser(String login, String password) {
        TokenResponse tokenResponse = authServiceClient.login(new AuthenticateRq(login, password));
        log.warn("TokenResponse: {}", tokenResponse);
        return tokenResponse != null;
    }
}