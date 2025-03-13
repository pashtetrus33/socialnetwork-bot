package ru.skillbox.social_network_bot.client;

import feign.QueryMap;
import feign.RequestLine;
import ru.skillbox.social_network_bot.dto.PagePostDto;
import ru.skillbox.social_network_bot.dto.PostSearchDto;


public interface PostServiceClient {

    @RequestLine("GET")
    PagePostDto getAll(@QueryMap PostSearchDto postSearchDto);
}