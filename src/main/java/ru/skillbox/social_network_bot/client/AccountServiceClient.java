package ru.skillbox.social_network_bot.client;

import feign.Param;
import feign.RequestLine;
import ru.skillbox.social_network_bot.dto.AccountDto;


import java.util.UUID;


public interface AccountServiceClient {

    @RequestLine("GET /{id}")
    AccountDto getAccountById(@Param("id") UUID id);
}