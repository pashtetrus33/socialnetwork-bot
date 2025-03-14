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
import ru.skillbox.social_network_bot.client.PostServiceClient;
import ru.skillbox.social_network_bot.dto.*;
import ru.skillbox.social_network_bot.entity.TelegramUser;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TelegramBotService extends TelegramWebhookBot {

    private final AuthServiceClient authServiceClient;

    private final Map<Long, UserSession> userSessions = new HashMap<>();
    private final String botUsername;
    private final TelegramUserService telegramUserService;
    private final PostServiceClient postServiceClient;
    private final TokenService tokenService;
    private String token;


    public TelegramBotService(@Value("${telegram.bot-token}") String botToken, AuthServiceClient authServiceClient,
                              @Value("${telegram.bot-username}") String botUsername, TelegramUserService telegramUserService, PostServiceClient postServiceClient, TokenService tokenService) {
        super(botToken);
        this.authServiceClient = authServiceClient;
        this.botUsername = botUsername;
        this.telegramUserService = telegramUserService;
        this.postServiceClient = postServiceClient;
        this.tokenService = tokenService;
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
                    sendMessage(chatId, "Hi! How can I help you :)");
                    break;

                case "/login":
                    // Начало процесса логина
                    if (token != null) {
                        token = null;
                        sendMessage(chatId, "Current access token is deleted.");
                    }

                    userSession.setState(UserState.AWAITING_LOGIN);
                    sendMessage(chatId, "Please enter your login:");
                    break;

                case "/get_friends_posts":
                    getFriends(userSession, chatId, true);
                    break;

                case "/get_all_posts":
                    getFriends(userSession, chatId, false);
                    break;


                case "/validate":

                    if (tokenValid(token)) {
                        sendMessage(chatId, "Token is valid.");
                    } else {
                        sendMessage(chatId, "Token is not valid. Please login first.");
                        userSession.setState(UserState.DEFAULT);
                    }
                    break;

                default:
                    // Обработка ввода логина и пароля
                    if (userSession.getState() == UserState.AWAITING_LOGIN) {
                        userSession.setLogin(text);
                        userSession.setState(UserState.AWAITING_PASSWORD);
                        sendMessage(chatId, "Please enter your password:");
                    } else if (userSession.getState() == UserState.AWAITING_PASSWORD) {
                        userSession.setPassword(text);
                        String login = userSession.getLogin();
                        String password = userSession.getPassword();

                        if (authenticateUser(login, password)) {
                            sendMessage(chatId, "Successful authorization!\nAccess token: " + token);
                            tokenService.setToken(token);
                            userSession.setState(UserState.AUTHENTICATED);
                            TelegramUser telegramUser = TelegramUser.builder()
                                    .chatId(chatId)
                                    .login(login)
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .username(user.getUserName())
                                    .phoneNumber(phoneNumber)
                                    .languageCode(user.getLanguageCode())
                                    .isBot(user.getIsBot()) // Или false, если хотите явно указать
                                    .isActive(true) // Предположим, что по умолчанию активный
                                    .build();

                            // Проверяем, существует ли пользователь с таким chatId
                            TelegramUser existingUser = telegramUserService.findByChatId(telegramUser.getChatId());

                            if (existingUser != null) {
                                // Если пользователь с таким chatId уже существует, обновляем его данные
                                existingUser.setLogin(telegramUser.getLogin());
                                existingUser.setFirstName(telegramUser.getFirstName());
                                existingUser.setLastName(telegramUser.getLastName());
                                existingUser.setUsername(telegramUser.getUsername());
                                existingUser.setPhoneNumber(telegramUser.getPhoneNumber());
                                existingUser.setLanguageCode(telegramUser.getLanguageCode());
                                existingUser.setIsBot(telegramUser.getIsBot());
                                existingUser.setIsActive(telegramUser.getIsActive());
                                existingUser.setUpdatedAt(LocalDateTime.now());

                                // Сохраняем изменения в базу
                                telegramUserService.save(existingUser);
                                log.info("Telegram user updated and saved: {}", existingUser);

                            } else {
                                // Если пользователя с таким chatId нет, создаем нового
                                telegramUserService.save(telegramUser);
                                log.info("New telegram user created: {}", telegramUser);
                            }


                        } else {
                            sendMessage(chatId, "Login or password is incorrect. Or auth service is unavailable. Please try again.");
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
            log.error("Error while sending message: {}", e.getMessage());
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

    private PagePostDto getPosts(PostSearchDto postSearchDto) {
        try {
            log.info("Getting posts for {}", postSearchDto);
            return postServiceClient.getAll(postSearchDto);

        } catch (FeignException e) {
            log.error("Freign client exception: {}", e.getMessage());
            return null;
        }
    }

    public String formatPostMessage(PostDto postDto) {
        StringBuilder message = new StringBuilder();
        message.append("📅 **Publish date:** ").append(postDto.getPublishDate()).append("\n");
        message.append("📝 **Title:** ").append(postDto.getTitle()).append("\n");
        message.append("✍️ **Text:**\n").append(postDto.getPostText()).append("\n");

        if (postDto.getImagePath() != null) {
            message.append("🖼 **Image:** ").append(postDto.getImagePath()).append("\n");
        }

        return message.toString();
    }

    private void getFriends(UserSession userSession, Long chatId, Boolean withFriends) {
        // Запрос на получение постов друзей
        if (isAuthenticated(userSession)) {
            sendMessage(chatId, "Ок. Let's go for the friends posts...");

            PostSearchDto postSearchDto = PostSearchDto.builder()
                    .isDeleted(false)
                    .withFriends(withFriends)
                    .build();

            PagePostDto pagePostDto = getPosts(postSearchDto);
            log.info("PagePostDto: {}", pagePostDto);

            if (pagePostDto != null) {

                // Выводим информацию о странице
                String pageInfo = String.format(
                        """
                                Page: %d of %d
                                Total elements: %d
                                Total pages: %d
                                Page size: %d
                                First page: %b
                                Last page: %b
                                """,
                        pagePostDto.getNumber(),              // Номер текущей страницы
                        pagePostDto.getTotalPages(),          // Общее количество страниц
                        pagePostDto.getTotalElements(),       // Общее количество элементов
                        pagePostDto.getTotalPages(),          // Общее количество страниц
                        pagePostDto.getSize(),                // Размер страницы (количество элементов на странице)
                        pagePostDto.getFirst(),               // Это первая страница?
                        pagePostDto.getLast()                 // Это последняя страница?
                );

                // Отправляем информацию о странице
                sendMessage(chatId, pageInfo);

                pagePostDto.getContent().stream()
                        .map(this::formatPostMessage)
                        .forEach(message -> sendMessage(chatId, message));

            } else {
                sendMessage(chatId, "Posts not found or post service is unavailable. Sucks!");
            }

        } else {
            sendMessage(chatId, "Please login first.");
        }
    }
}