package ru.skillbox.social_network_bot.client;

import feign.Headers;
import feign.RequestLine;
import ru.skillbox.social_network_bot.dto.PagePostDto;
import ru.skillbox.social_network_bot.dto.PostSearchDto;


public interface PostServiceClient {

    @RequestLine("GET /")
    PagePostDto getAll(PostSearchDto postSearchDto);
}