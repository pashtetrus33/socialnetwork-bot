package ru.skillbox.social_network_bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.skillbox.social_network_bot.config.LocalDateTimeDeserializer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@JsonIgnoreProperties(ignoreUnknown = true)  // Игнорировать неизвестные поля
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class PostDto {

    private UUID id;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime time;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timeChanged;

    private UUID authorId;

    @NotNull(message = "Title must not be null")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private PostType type;

    @NotNull(message = "Post text must not be null")
    private String postText;

    @Builder.Default
    private Boolean isBlocked = false;

    @Builder.Default
    private Boolean isDeleted = false;

    @Builder.Default
    private Long commentsCount = 0L;

    @Size(max = 50, message = "Tags list size must not exceed 50")
    private List<TagDto> tags;

    @Builder.Default
    private Long likeAmount = 0L;

    @Builder.Default
    private List<ReactionDto.ReactionInfo> reactionType = List.of(
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(ReactionType.DELIGHT.getName())).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(ReactionType.MALICE.getName())).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(ReactionType.HEART.getName())).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(ReactionType.SADNESS.getName())).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(ReactionType.FUNNY.getName())).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(ReactionType.WOW.getName())).count(0L).build()
    );

    private String myReaction;

    @Builder.Default
    private Boolean myLike = false;

    @Size(max = 512, message = "Image path must not exceed 512 characters")
    private String imagePath;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime publishDate;
}