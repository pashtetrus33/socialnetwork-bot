package ru.skillbox.social_network_bot.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.skillbox.social_network_bot.client.AuthServiceClient;
import ru.skillbox.social_network_bot.dto.AuthenticateRq;
import ru.skillbox.social_network_bot.dto.TokenResponse;
import ru.skillbox.social_network_bot.dto.UserSession;
import ru.skillbox.social_network_bot.dto.UserState;
import ru.skillbox.social_network_bot.entity.TelegramUser;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TelegramBotService extends TelegramWebhookBot {

    private final AuthServiceClient authServiceClient;

    private final Map<Long, UserSession> userSessions = new HashMap<>();
    private final String botUsername;
    private final TelegramUserService telegramUserService;
    private String token;

    public TelegramBotService(@Value("${telegram.bot-token}") String botToken, AuthServiceClient authServiceClient,
                              @Value("${telegram.bot-username}") String botUsername, TelegramUserService telegramUserService) {
        super(botToken);
        this.authServiceClient = authServiceClient;
        this.botUsername = botUsername;
        this.telegramUserService = telegramUserService;
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

            String phoneNumber = "No phone number";
            User user = update.getMessage().getFrom();
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            if (update.hasMessage() && update.getMessage().hasContact()) {
                phoneNumber = update.getMessage().getContact().getPhoneNumber();
            }

            UserSession userSession = userSessions.getOrDefault(chatId, new UserSession(chatId));
            userSession.setChatId(chatId);

            switch (text) {
                case "/start":
                    // Приветствие
                    sendMessage(chatId, "Привет! Чем могу помочь?");
                    break;

                case "/login":
                    // Начало процесса логина
                    userSession.setState(UserState.AWAITING_LOGIN);
                    sendMessage(chatId, "Пожалуйста, введите ваш логин.");
                    break;

                case "/get_friends_posts":
                    // Запрос на получение постов друзей
                    if (isAuthenticated(userSession)) {
                        sendMessage(chatId, "Ок. Идем за постами друзей.");
                    } else {
                        sendMessage(chatId, "Пожалуйста, авторизуйтесь для получения постов.");
                    }
                    break;


                case "/validate":

                    if (tokenValid(token)) {
                        sendMessage(chatId, "Токен валидный.");
                    } else {
                        sendMessage(chatId, "Токен не валидный.Пожалуйста, авторизуйтесь для получения постов.");
                        userSession.setState(UserState.DEFAULT);
                    }
                    break;

                default:
                    // Обработка ввода логина и пароля
                    if (userSession.getState() == UserState.AWAITING_LOGIN) {
                        userSession.setLogin(text);
                        userSession.setState(UserState.AWAITING_PASSWORD);
                        sendMessage(chatId, "Теперь введите ваш пароль.");
                    } else if (userSession.getState() == UserState.AWAITING_PASSWORD) {
                        userSession.setPassword(text);
                        String login = userSession.getLogin();
                        String password = userSession.getPassword();

                        if (authenticateUser(login, password)) {
                            sendMessage(chatId, "Авторизация успешна!\nТокен: " + token);
                            userSession.setState(UserState.AUTHENTICATED);
                            TelegramUser telegramUser = TelegramUser.builder()
                                    .chatId(chatId)
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .username(user.getUserName())
                                    .phoneNumber(phoneNumber)
                                    .languageCode(user.getLanguageCode())
                                    .isBot(user.getIsBot()) // Или false, если хотите явно указать
                                    .isActive(true) // Предположим, что по умолчанию активный
                                    .build();

                            telegramUserService.create(telegramUser);
                            log.info("New telegram user created: {}", telegramUser);

                        } else {
                            sendMessage(chatId, "Неверный логин или пароль. Попробуйте еще раз.");
                            userSession.setState(UserState.DEFAULT);
                        }
                    }
                    break;
            }

            userSessions.put(chatId, userSession);
        }
        return null;
    }

    private boolean isAuthenticated(UserSession userSession) {
        return userSession.getState() == UserState.AUTHENTICATED;
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

        try {
            AuthenticateRq authenticateRq = new AuthenticateRq();
            authenticateRq.setEmail(login);
            authenticateRq.setPassword(password);

            log.info("AuthenticateRq: {}", authenticateRq);

            token = authServiceClient.login(authenticateRq).getAccessToken();

        } catch (FeignException e) {
            return false;
        }

        return true;
    }

    private boolean tokenValid(String token) {
        try {
            return authServiceClient.validateToken(token);

        } catch (FeignException e) {
            return false;
        }
    }
}