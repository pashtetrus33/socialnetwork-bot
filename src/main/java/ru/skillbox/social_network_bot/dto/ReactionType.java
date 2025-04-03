package ru.skillbox.social_network_bot.dto;

public enum ReactionType {
    DELIGHT,
    MALICE,
    HEART,
    SADNESS,
    FUNNY,
    WOW;

    public String getName() {
        return name().toLowerCase();
    }
}