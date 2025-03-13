package ru.skillbox.social_network_bot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class AuthenticateRq {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}