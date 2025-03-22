package ru.skillbox.social_network_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
    private String firstName;
    private String lastName;
    private ZonedDateTime regDate;
    private LocalDate birthDate;
    private String messagePermission;
    private ZonedDateTime lastOnlineTime;
    private boolean isOnline;
    private boolean isBlocked;
    private boolean isDeleted;
    private String photoId;
    private String photoName;
    private ZonedDateTime createdOn;
    private ZonedDateTime updatedOn;
    private String password;
    private String emojiStatus;
}