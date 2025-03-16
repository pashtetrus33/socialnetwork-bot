package ru.skillbox.social_network_bot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.skillbox.social_network_bot.client.AccountServiceClient;
import ru.skillbox.social_network_bot.client.AuthServiceClient;
import ru.skillbox.social_network_bot.client.PostServiceClient;
import ru.skillbox.social_network_bot.service.TokenService;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class FeignClientsConfig {

    @Value("${gateway.api.url}")
    private String gatewayApiUrl;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final TokenService tokenService;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Accept", "application/json");

            // Получаем токен КАЖДЫЙ РАЗ перед отправкой запроса
            String token = tokenService.getToken();

            if (token != null && !token.isEmpty()) {
                requestTemplate.header("Authorization", "Bearer " + token);
            } else {
                log.warn("Токен отсутствует! Запрос может завершиться ошибкой 401.");
            }
        };
    }


    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }


    // Бин для AuthServiceClient
    @Bean
    public AuthServiceClient authServiceClient() {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new feign.slf4j.Slf4jLogger(AuthServiceClient.class))
                .logLevel(Logger.Level.FULL)
                .target(AuthServiceClient.class, gatewayApiUrl + "/api/v1/auth");
    }


    @Bean
    public PostServiceClient postServiceClient() {
        return Feign.builder()
                .encoder(jacksonEncoder())
                .decoder(jacksonDecoder())
                .logger(new feign.slf4j.Slf4jLogger(PostServiceClient.class))
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(requestInterceptor())
                .target(PostServiceClient.class, gatewayApiUrl + "/api/v1/post");
    }

    @Bean
    public AccountServiceClient accountServiceClient() {
        return Feign.builder()
                .encoder(jacksonEncoder())
                .decoder(jacksonDecoder())
                .logger(new feign.slf4j.Slf4jLogger(PostServiceClient.class))
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(requestInterceptor())
                .target(AccountServiceClient.class, gatewayApiUrl + "/api/v1/account");
    }

    // Бин для Jackson Encoder
    @Bean
    public Encoder jacksonEncoder() {
        return new JacksonEncoder(objectMapper);
    }

    // Бин для Jackson Decoder
    @Bean
    public Decoder jacksonDecoder() {

        return new JacksonDecoder(objectMapper);
    }
}