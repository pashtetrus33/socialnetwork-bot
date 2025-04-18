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
import ru.skillbox.social_network_bot.client.AccountServiceClient;
import ru.skillbox.social_network_bot.client.AuthServiceClient;
import ru.skillbox.social_network_bot.client.PostServiceClient;
import ru.skillbox.social_network_bot.dto.*;
import ru.skillbox.social_network_bot.entity.TelegramUser;
import ru.skillbox.social_network_bot.security.JwtUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TelegramBotService extends TelegramWebhookBot {

    public static final String PLEASE_LOGIN_FIRST = "Please login first.";
    private final AuthServiceClient authServiceClient;
    private final Map<Long, UserSession> userSessions = new HashMap<>();
    private final String botUsername;
    private final TelegramUserService telegramUserService;
    private final PostServiceClient postServiceClient;
    private final TokenService tokenService;
    private final AccountServiceClient accountServiceClient;
    private final JwtUtil jwtUtil;


    public TelegramBotService(@Value("${telegram.bot-token}") String botToken, AuthServiceClient authServiceClient,
                              @Value("${telegram.bot-username}") String botUsername, TelegramUserService telegramUserService, PostServiceClient postServiceClient,
                              TokenService tokenService, JwtUtil jwtUtil, AccountServiceClient accountServiceClient) {
        super(botToken);
        this.authServiceClient = authServiceClient;
        this.botUsername = botUsername;
        this.telegramUserService = telegramUserService;
        this.postServiceClient = postServiceClient;
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
        this.accountServiceClient = accountServiceClient;
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

        String accessToken;

        if (update.hasMessage() && update.getMessage().hasText()) {

            String phoneNumber = "No phone number";
            User user = update.getMessage().getFrom();
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            tokenService.setChatId(chatId);

            if (update.hasMessage() && update.getMessage().hasContact()) {
                phoneNumber = update.getMessage().getContact().getPhoneNumber();
            }

            UserSession userSession = userSessions.getOrDefault(chatId, new UserSession(chatId));
            userSession.setChatId(chatId);

            switch (text) {
                case "/start":
                    // Приветствие и список команд
                    sendMessage(chatId, """
                            Привет! 😊 Я ваш бот и готов помочь!
                            
                            Список доступных команд:
                            /login - Вход без пароля
                            /signin - Вход по паролю
                            /create - Создать пост
                            /friends_posts - Посты друзей
                            /my_posts - Мои посты
                            /get_all - Все посты
                            /validate - Проверить текущий токен
                            
                            Чем могу помочь? 🙂
                            """);
                    break;


                case "/login":
                    // Начало процесса логина
                    if (tokenService.getTokens().get(chatId) != null) {
                        tokenService.getTokens().put(chatId, null);
                        sendMessage(chatId, "Current access token is deleted for chatId " + chatId);
                    }

                    userSession.setState(UserState.AWAITING_LOGIN_WITHOUTPASSWORD);
                    sendMessage(chatId, "Please enter your login:");
                    break;

                case "/signin":
                    // Начало процесса логина
                    if (tokenService.getTokens().get(chatId) != null) {
                        tokenService.getTokens().put(chatId, null);
                        sendMessage(chatId, "Current access token is deleted for chatId " + chatId);
                    }

                    userSession.setState(UserState.AWAITING_LOGIN);
                    sendMessage(chatId, "Please enter your login:");
                    break;

                case "/friends_posts":
                    if (isAuthenticated(userSession, chatId)) {
                        userSession.setState(UserState.AWAITING_PAGE);
                        userSession.setWithFriends(true);
                        sendMessage(chatId, "Please enter page:");
                    } else {
                        sendMessage(chatId, PLEASE_LOGIN_FIRST);
                    }
                    break;

                case "/my_posts":
                    getOwn(userSession, chatId);
                    break;

                case "/get_all":
                    if (isAuthenticated(userSession, chatId)) {
                        userSession.setState(UserState.AWAITING_PAGE);
                        userSession.setWithFriends(false);
                        sendMessage(chatId, "Please enter page:");
                    } else {
                        sendMessage(chatId, PLEASE_LOGIN_FIRST);
                    }

                    break;

                case "/create":
                    create(userSession, chatId);
                    break;

                case "/users":
                    showUsers(userSession, chatId);
                    break;

                case "/validate":

                    accessToken = tokenService.getTokens().get(chatId);
                    sendMessage(chatId, "Token validation for token " + accessToken + " chatId " + chatId);

                    if (tokenValid(accessToken)) {
                        sendMessage(chatId, "Token is valid for chatId " + chatId);
                        try {
                            sendMessage(chatId, "Username: " + jwtUtil.extractUsername(accessToken));
                            sendMessage(chatId, "UserId: " + jwtUtil.extractUserId(accessToken));
                        } catch (IllegalArgumentException e) {
                            log.error(e.getMessage());
                            sendMessage(chatId, "Invalid token. Please try again.");
                            userSession.setState(UserState.DEFAULT);
                            userSession.setAuthenticated(false);
                        }
                    } else {
                        sendMessage(chatId, "Token is not valid. Please login first.");
                        userSession.setState(UserState.DEFAULT);
                    }
                    break;

                default:
                    if (userSession.getState() == UserState.AWAITING_PAGE) {
                        try {
                            userSession.setPage(Integer.parseInt(text));
                            userSession.setState(UserState.AWAITING_SIZE);
                            sendMessage(chatId, "Please enter page size:");

                        } catch (NumberFormatException e) {
                            sendMessage(update.getMessage().getChatId(), "Пожалуйста, введите корректный номер страницы.");
                            userSession.setState(UserState.DEFAULT);
                            userSession.setWithFriends(false);
                        }
                    } else if (userSession.getState() == UserState.AWAITING_SIZE) {
                        try {
                            userSession.setSize(Integer.parseInt(text));
                            getPosts(userSession, chatId);

                        } catch (NumberFormatException e) {
                            sendMessage(update.getMessage().getChatId(), "Пожалуйста, введите корректное число элементов на странице.");
                            userSession.setState(UserState.DEFAULT);
                            userSession.setWithFriends(false);
                        }
                    }
                    if (userSession.getState() == UserState.AWAITING_TITLE) {
                        userSession.setTitle(text);
                        userSession.setState(UserState.AWAITING_TEXT);
                        sendMessage(chatId, "Please enter text:");
                    } else if (userSession.getState() == UserState.AWAITING_TEXT) {

                        PostDto postDto = PostDto.builder()
                                .title(userSession.getTitle())
                                .postText(text)
                                .build();

                        boolean isCreated = createPost(postDto);

                        if (isCreated) {
                            sendMessage(chatId, "Post is created.");
                        } else {
                            sendMessage(chatId, "Post is not created.");
                        }
                        userSession.setState(UserState.DEFAULT);
                    }

                    // Обработка ввода логина и пароля
                    if (userSession.getState() == UserState.AWAITING_LOGIN) {

                        userSession.setLogin(text);

                        userSession.setState(UserState.AWAITING_PASSWORD);
                        sendMessage(chatId, "Please enter your password:");


                    } else if (userSession.getState() == UserState.AWAITING_LOGIN_WITHOUTPASSWORD) {

                        userSession.setLogin(text);

                        accessToken = authenticateUser(userSession.getLogin(), "fake");

                        if (accessToken != null) {
                            sendMessage(chatId, "Successful authorization without password! Token: " + accessToken);
                            tokenService.getTokens().put(chatId, accessToken);
                            userSession.setAuthenticated(true);

                        } else {
                            userSession.setState(UserState.DEFAULT);
                            sendMessage(chatId, "Failed authorization without password!");
                        }

                    } else if (userSession.getState() == UserState.AWAITING_PASSWORD) {
                        userSession.setPassword(text);
                        String login = userSession.getLogin();
                        String password = userSession.getPassword();

                        accessToken = authenticateUser(login, password);

                        if (accessToken != null) {
                            sendMessage(chatId, "Successful authorization!\nAccess token: " + accessToken);
                            tokenService.getTokens().put(chatId, accessToken);
                            userSession.setAuthenticated(true);
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


    private boolean isAuthenticated(UserSession userSession, Long chatId) {
        sendMessage(chatId, "Token validation...");
        return userSession.isAuthenticated() && tokenValid(tokenService.getTokens().get(chatId));
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

    private String authenticateUser(String login, String password) {

        String accessToken;

        try {
            AuthenticateRq authenticateRq = new AuthenticateRq();
            authenticateRq.setEmail(login);
            authenticateRq.setPassword(password);

            log.info("AuthenticateRq: {}", authenticateRq);

            accessToken = authServiceClient.login(authenticateRq).getAccessToken();

        } catch (FeignException e) {
            return null;
        }

        return accessToken;
    }

    private boolean tokenValid(String token) {
        try {
            return authServiceClient.validateToken(token);

        } catch (FeignException e) {
            log.error("Freign client exception while token validation: {}", e.getMessage());
            return false;
        }
    }


    private PagePostDto getPosts(PostSearchDto postSearchDto, Integer page, Integer size) {
        try {
            log.info("Getting posts for {}", postSearchDto);
            return postServiceClient.getAll(postSearchDto.getIsDeleted(), postSearchDto.getWithFriends(), null, "publishDate", "asc", page, size);

        } catch (FeignException e) {
            log.error("Freign exception: {}", e.getMessage());
            return null;
        }
    }


    private boolean createPost(PostDto postDto) {
        try {
            log.info("Creating post {}", postDto);
            postServiceClient.create(postDto);
            return true;

        } catch (FeignException e) {
            log.error("Freign client exception: {}", e.getMessage());
            return false;
        }
    }

    private void getPosts(UserSession userSession, Long chatId) {

        if (isAuthenticated(userSession, chatId)) {

            if (Boolean.TRUE.equals(userSession.isWithFriends())) {
                sendMessage(chatId, "Ок. Let's go for the friends posts...");
            } else {
                sendMessage(chatId, "Ок. Let's go for the all posts...");
            }


            PostSearchDto postSearchDto = PostSearchDto.builder()
                    .isDeleted(null)
                    .withFriends(userSession.isWithFriends())
                    .build();

            PagePostDto pagePostDto = getPosts(postSearchDto, userSession.getPage(), userSession.getSize());
            log.info("PagePostDto: {}", pagePostDto);

            userFormat(chatId, pagePostDto);

        } else {
            sendMessage(chatId, PLEASE_LOGIN_FIRST);
        }
    }

    private void create(UserSession userSession, Long chatId) {

        if (isAuthenticated(userSession, chatId)) {
            userSession.setState(UserState.AWAITING_TITLE);
            sendMessage(chatId, "Please enter title:");

        } else {
            sendMessage(chatId, PLEASE_LOGIN_FIRST);
        }
    }

    private void getOwn(UserSession userSession, Long chatId) {

        if (isAuthenticated(userSession, chatId)) {

            String userName;
            UUID userId;

            try {
                String accessToken = tokenService.getTokens().get(chatId);
                userName = jwtUtil.extractUsername(accessToken);
                userId = jwtUtil.extractUserId(accessToken);
                sendMessage(chatId, "Username: " + userName);
                sendMessage(chatId, "UserId: " + userId);
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage());
                sendMessage(chatId, "Invalid token. Please try again.");
                userSession.setState(UserState.DEFAULT);
                userSession.setAuthenticated(false);
                return;
            }
            sendMessage(chatId, "Ок. Let's go for my own posts...");

            PagePostDto pagePostDto = postServiceClient.getAll(false, null, Collections.singletonList(userId), "publishDate", "asc", 0, 20);

            userFormat(chatId, pagePostDto);


        } else {
            sendMessage(chatId, PLEASE_LOGIN_FIRST);
        }
    }

    private void userFormat(Long chatId, PagePostDto pagePostDto) {
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
    }

    public String formatPostMessage(PostDto postDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");

        StringBuilder message = new StringBuilder();
        message.append("📅 *Дата публикации:* ").append(postDto.getPublishDate().format(formatter)).append("\n\n");
        message.append("📝 *").append(postDto.getTitle()).append("*\n\n");

        // Очистка HTML-тегов, можно улучшить
        String postText = postDto.getPostText().replaceAll("<[^>]*>", "").trim();
        message.append("✍️ ").append(postText).append("\n\n");

        if (postDto.getImagePath() != null) {
            message.append("🖼 [Фото](").append(postDto.getImagePath()).append(")\n\n");
        }

        if (postDto.getTags() != null && !postDto.getTags().isEmpty()) {
            String tags = postDto.getTags().stream()
                    .map(TagDto::name) // Используем метод name() из record
                    .collect(Collectors.joining(", "));
            message.append("🖼 Тэги: ").append(tags).append("\n\n");
        }

        message.append("✍️ Комментариев: ").append(postDto.getCommentsCount()).append("\n\n");


        AccountDto accountDto = getAccountInfo(postDto.getAuthorId());
        log.warn("Trying to get account info {}", postDto.getAuthorId());

        if (accountDto != null) {
            message.append("🧑 *Автор:* ").append(accountDto.getFirstName()).append(" ").append(accountDto.getLastName()).append("\n\n")
                    .append("📧 *Email:* ").append(accountDto.getEmail()).append("\n")
                    .append("📍 *Город:* ").append(accountDto.getCity()).append("\n");


        } else {
            message.append("🧑 *Автор:* ").append(postDto.getAuthorId()).append("*\n\n");
        }
        message.append("━━━━━━━━━━━━━━━\n");

        return message.toString();
    }

    private AccountDto getAccountInfo(UUID accountId) {
        try {
            log.info("Getting account info {}", accountId);
            return accountServiceClient.getAccountById(accountId);
        } catch (FeignException e) {
            log.error("Ошибка Feign клиента при получении информации об аккаунте: {}", e.getMessage());
            return null;
        }
    }


    private void showUsers(UserSession userSession, Long chatId) {
        List<TelegramUser> users = telegramUserService.getAll();

        if (users.isEmpty()) {
            sendMessage(chatId, "🚫 Пользователи не найдены.");
            return;
        }

        StringBuilder message = new StringBuilder("👥 *Список пользователей:*\n");
        message.append("━━━━━━━━━━━━━━━━━━━━\n");

        userFormat(userSession, users, message);

        sendMessage(chatId, message.toString());
    }

    private static void userFormat(UserSession userSession, List<TelegramUser> users, StringBuilder message) {
        for (TelegramUser user : users) {
            UserState state = userSession.getState();
            message.append(String.format("""
                            🆔 *ID:* %s
                            🗣 *Имя:* %s %s
                            🔹 *Логин:* %s
                            📟 *Username:* %s
                            📞 *Телефон:* %s
                            🔄 *Статус:* %s
                            🌍 *Язык:* %s
                            🤖 *Бот:* %s
                            🔐 *Аутентификация:* %s
                            ⚙ *Состояние:* %s
                            📅 *Создан:* %s
                            🕒 *Обновлен:* %s
                            ━━━━━━━━━━━━━━━━━━━━
                            """,
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName() != null ? user.getLastName() : "",
                    user.getLogin(),
                    user.getUsername() != null ? user.getUsername() : "N/A",
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A",
                    Boolean.TRUE.equals(user.getIsActive()) ? "✅ Активен" : "❌ Неактивен",
                    user.getLanguageCode() != null ? user.getLanguageCode() : "N/A",
                    user.getIsBot() != null && user.getIsBot() ? "🤖 Да" : "👤 Нет",
                    userSession.isAuthenticated() ? "✅ Да" : "❌ Нет",
                    state != null ? state.name() : "UNKNOWN",
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            ));
        }
    }
}