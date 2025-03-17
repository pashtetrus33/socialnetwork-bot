package ru.skillbox.social_network_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    private Long chatId;
    private UserState state = UserState.DEFAULT;
    private String login;
    private String password;
    private String title;
    private String postText;
    private boolean isAuthenticated;


    public UserSession(Long chatId) {
        this.chatId = chatId;
    }
}