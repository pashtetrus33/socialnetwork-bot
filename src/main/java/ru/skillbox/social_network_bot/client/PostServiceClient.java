package ru.skillbox.social_network_bot.client;

import feign.RequestLine;
import ru.skillbox.social_network_bot.dto.PagePostDto;


public interface PostServiceClient {

    @RequestLine("GET /friendId")
    PagePostDto getAll();
}