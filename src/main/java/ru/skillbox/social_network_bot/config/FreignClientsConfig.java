package ru.skillbox.social_network_bot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.skillbox.social_network_bot.client.AuthServiceClient;
import ru.skillbox.social_network_bot.client.PostServiceClient;
import ru.skillbox.social_network_bot.service.TelegramBotService;

@RequiredArgsConstructor
@Configuration
public class FreignClientsConfig {

    private final TelegramBotService telegramBotService;

    @Value("${gateway.api.url}")
    private String gatewayApiUrl;

    ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Добавляем заголовки, например, для авторизации
            requestTemplate.header("Accept", "application/json");
            String token = telegramBotService.getToken();
            requestTemplate.header("Authorization", "Bearer " + token);
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