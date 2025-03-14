package ru.skillbox.social_network_bot.dto;

public enum UserState {
    AWAITING_LOGIN,
    AWAITING_PASSWORD,
    AUTHENTICATED,
    AWAITING_TITLE,
    AWAITING_TEXT, DEFAULT
}