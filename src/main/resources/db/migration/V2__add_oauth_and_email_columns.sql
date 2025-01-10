-- Добавляем новые колонки
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS email VARCHAR(255) UNIQUE,
ADD COLUMN IF NOT EXISTS provider VARCHAR(50),
ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);

-- Обновляем существующие записи
UPDATE users 
SET provider = 'local',
    email = CONCAT(username, '@example.com')
WHERE provider IS NULL;

-- Делаем колонки NOT NULL после обновления данных
ALTER TABLE users 
ALTER COLUMN provider SET NOT NULL; 