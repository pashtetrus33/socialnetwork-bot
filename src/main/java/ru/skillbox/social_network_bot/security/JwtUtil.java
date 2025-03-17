package ru.skillbox.social_network_bot.security;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {

    // Извлечение имени пользователя
    public String extractUsername(String token) {
        JSONObject json = extractPayload(token);

        String username = json.optString("sub", null);
        if (username == null) {
            throw new IllegalArgumentException("Invalid JWT token: missing or empty 'sub' field");
        }

        return username;
    }

    public UUID extractUserId(String token) {
        // Извлечение полезной нагрузки из JWT
        JSONObject json = extractPayload(token);

        // Получаем строку userId
        String userIdStr = json.optString("accountId", "");

        // Проверяем, если userId пустой, выбрасываем исключение
        if (userIdStr.isEmpty()) {
            throw new IllegalArgumentException("Invalid JWT token: missing or empty 'userId' field");
        }

        // Преобразуем строку в UUID
        return UUID.fromString(userIdStr);
    }

    // Извлечение ролей
    public List<String> extractRoles(String token) {
        JSONObject json = extractPayload(token);

        // Если поле roles присутствует, преобразуем его в список строк
        return json.optJSONArray("roles") != null
                ? json.optJSONArray("roles").toList().stream()
                .map(Object::toString)
                .toList()
                : List.of(); // Возвращаем пустой список, если ролей нет
    }

    // Извлечение полезной нагрузки (payload) из токена
    private JSONObject extractPayload(String token) {
        String[] chunks = token.split("\\.");
        if (chunks.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token: incorrect format");
        }

        try {
            String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
            return new JSONObject(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode JWT token", e);
        }
    }
}