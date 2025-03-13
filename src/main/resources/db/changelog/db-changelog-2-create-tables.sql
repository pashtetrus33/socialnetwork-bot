-- Создание таблицы telegram_users в схеме schema_bot
CREATE TABLE IF NOT EXISTS schema_bot.telegram_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    chat_id BIGINT UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    username VARCHAR(255),
    is_active BOOLEAN NOT NULL,
    language_code VARCHAR(10),
    is_bot BOOLEAN,
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );