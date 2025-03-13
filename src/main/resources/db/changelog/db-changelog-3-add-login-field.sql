-- Добавление нового поля login в таблицу telegram_users
ALTER TABLE schema_bot.telegram_users
    ADD COLUMN login VARCHAR(255) UNIQUE NOT NULL;