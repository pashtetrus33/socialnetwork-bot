package ru.skillbox.social_network_bot.dto;

public enum UserState {
    AWAITING_LOGIN,
    AWAITING_PASSWORD,
    AWAITING_TITLE,
    AWAITING_TEXT,
    AWAITING_PAGE,
    AWAITING_SIZE,
    AWAITING_LOGIN_WITHOUTPASSWORD, DEFAULT
}