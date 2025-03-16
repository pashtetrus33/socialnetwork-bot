package ru.skillbox.social_network_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    private UUID id;
    private String email;
    private String phone;
    private String photo;
    private String about;
    private String city;
    private String country;
    private ru.skillbox.social_network_bot.dto.Status statusCode;
    private String firstName;
    private String lastName;
    private LocalDateTime birthDate;
    private String messagePermission;
    private LocalDateTime lastOnlineTime;
    private boolean isOnline;
    private boolean isBlocked;
    private boolean isDeleted;
    private String photoId;
    private String photoName;
}