package ru.skillbox.social_network_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillbox.social_network_bot.entity.TelegramUser;

import java.util.UUID;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, UUID> {
}
