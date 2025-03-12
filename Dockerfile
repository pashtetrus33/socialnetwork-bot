# Используем официальный образ OpenJDK 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл в контейнер (сборка должна быть в target/)
COPY target/social-network-bot-3.3.8.jar app.jar

EXPOSE 8443

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]