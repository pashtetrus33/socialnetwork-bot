package ru.skillbox.social_network_bot.client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import ru.skillbox.social_network_bot.dto.AuthenticateRq;
import ru.skillbox.social_network_bot.dto.TokenResponse;

public interface AuthServiceClient {

    @RequestLine("GET /validate?token={token}")
    Boolean validateToken(@Param("token") String token);


    @RequestLine("POST /login")
    @Headers("Content-Type: application/json")
    TokenResponse login(AuthenticateRq request);

}