package ru.skillbox.social_network_bot.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class ReactionDto {

    private Boolean active;
    private List<ReactionInfo> reactionsInfo;
    private Long quantity;
    private String reaction;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    public static class ReactionInfo {
        private String reactionType;
        private Long count;
    }
}
