package ru.skillbox.social_network_bot.client;

import feign.Param;
import feign.RequestLine;

public interface AuthServiceClient {

    @RequestLine("GET /validate?token={token}")
    Boolean validateToken(@Param("token") String token);


    //Метод получения токена
}