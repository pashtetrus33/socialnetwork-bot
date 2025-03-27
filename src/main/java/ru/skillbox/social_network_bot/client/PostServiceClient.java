package ru.skillbox.social_network_bot.client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import ru.skillbox.social_network_bot.dto.PagePostDto;
import ru.skillbox.social_network_bot.dto.PostDto;

import java.util.List;
import java.util.UUID;


public interface PostServiceClient {

    //@RequestLine("GET")
    //PagePostDto getAll(@QueryMap PostSearchDto postSearchDto);

    @RequestLine("GET ?isDeleted={isDeleted}&withFriends={withFriends}&accountIds={accountIds}&sort={sort}&direction={direction}&page={page}&size={size}")
    PagePostDto getAll(@Param("isDeleted") Boolean isDeleted,
                       @Param("withFriends") Boolean withFriends,
                       @Param("accountIds") List<UUID> accountIds,
                       @Param("sort") String sort,
                       @Param("direction") String direction,
                       @Param("page") int page,
                       @Param("size") int size);



    @RequestLine("POST")
    @Headers("Content-Type: application/json")
    void create(PostDto postDto);
}