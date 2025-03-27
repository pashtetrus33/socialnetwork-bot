package ru.skillbox.social_network_bot.client;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import ru.skillbox.social_network_bot.dto.PagePostDto;
import ru.skillbox.social_network_bot.dto.PostDto;
import ru.skillbox.social_network_bot.dto.PostSearchDto;
import ru.skillbox.social_network_bot.dto.SortDto;


public interface PostServiceClient {

    //@RequestLine("GET")
    //PagePostDto getAll(@QueryMap PostSearchDto postSearchDto);

    @RequestLine("GET")
    PagePostDto getAll(
            @QueryMap PostSearchDto postSearchDto,
            @QueryMap SortDto sortDto,
            @Param("page") int page,
            @Param("size") int size
    );

    @RequestLine("POST")
    @Headers("Content-Type: application/json")
    void create(PostDto postDto);
}