package ru.skillbox.social_network_bot.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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


    @JsonAlias({"pageNumber"}) // Поддерживает оба варианта: "number" и "pageNumber"
    @NotNull
    @Min(0)
    private Integer number;

    @JsonIgnore // Исключаем из сериализации, используется только для совместимости
    private Integer pageNumber;

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