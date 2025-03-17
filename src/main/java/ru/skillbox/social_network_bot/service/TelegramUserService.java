package ru.skillbox.social_network_bot.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_bot.entity.TelegramUser;
import ru.skillbox.social_network_bot.repository.TelegramUserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    public void save(TelegramUser telegramUser) {
        telegramUserRepository.save(telegramUser);
    }

    public TelegramUser findByChatId(Long chatId) {
        return telegramUserRepository.findByChatId(chatId).orElse(null);
    }

    public List<TelegramUser> getAll() {
        return telegramUserRepository.findAll();
    }
}