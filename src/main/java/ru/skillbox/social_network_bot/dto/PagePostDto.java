package ru.skillbox.social_network_bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.skillbox.social_network_bot.config.PageableDeserializer;
import ru.skillbox.social_network_bot.config.SortDeserializer;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)  // Игнорировать неизвестные поля
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagePostDto {

    @NotNull
    @PositiveOrZero
    private Long totalElements;

    @NotNull
    @PositiveOrZero
    private Integer totalPages;

    @NotNull
    @Min(0)
    private Integer number;

    @NotNull
    @Min(1)
    private Integer size;

    private List<PostDto> content;

    @JsonDeserialize(using = SortDeserializer.class)
    private Sort sort;

    @NotNull
    private Boolean first;

    @NotNull
    private Boolean last;

    @NotNull
    @Min(0)
    private Integer numberOfElements;

    @JsonDeserialize(using = PageableDeserializer.class)
    private Pageable pageable;

    @NotNull
    private Boolean empty;
}