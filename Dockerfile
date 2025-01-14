# Используем официальный образ Gradle для сборки
FROM gradle:8.5-jdk17 AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы build.gradle и src
COPY build.gradle settings.gradle ./
COPY src ./src

# Собираем приложение
RUN gradle build --no-daemon -x test

# Используем JRE для финального образа
FROM eclipse-temurin:17-jre-jammy

# Устанавливаем PostgreSQL клиент для проверки соединения
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Копируем JAR из этапа сборки
COPY --from=builder /app/build/libs/*.jar app.jar

# Создаем скрипт для запуска
RUN echo '#!/bin/sh\n\
echo "Waiting for database..."\n\
while ! pg_isready -h db -p 5432 -U postgres; do\n\
  sleep 1\n\
done\n\
echo "Database is ready!"\n\
echo "Starting application..."\n\
exec java -jar app.jar' > /app/start.sh && \
chmod +x /app/start.sh

# Открываем порт
EXPOSE 8080

# Запускаем приложение через скрипт
ENTRYPOINT ["/app/start.sh"] 