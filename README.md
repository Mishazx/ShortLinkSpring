# Сервис коротких ссылок

Веб-приложение на Spring Boot для создания и управления короткими ссылками. Сервис позволяет пользователям создавать короткие версии длинных URL-адресов с возможностью установки ограничений по количеству переходов и времени жизни ссылки, а также отслеживать статистику использования.

## Основные возможности

- 🔗 Создание коротких ссылок
- 📊 Отслеживание статистики переходов
- ⏰ Установка срока действия ссылок
- 🔄 Лимит количества переходов
- 👤 Личный кабинет пользователя
- 🌓 Темная/светлая тема
- 🔐 Авторизация через:
  - Email/Пароль
  - GitHub
  - Яндекс


## Руководство пользователя

### Регистрация
1. Нажмите кнопку "Регистрация" на главной странице
2. Заполните форму регистрации:
   - Имя пользователя
   - Email
   - Пароль
3. Или используйте быстрый вход через:
   - GitHub
   - Яндекс

### Вход в систему
У вас есть три способа войти:
1. По логину и паролю
2. Через GitHub
3. Через Яндекс

### Создание короткой ссылки
1. После входа нажмите "Создать ссылку"
2. Вставьте длинную ссылку
3. Настройте параметры:
   - Лимит переходов (0 для бесконечности)
   - Время жизни в часах (0 для бесконечности)
4. Нажмите "Создать"

### Управление ссылками
#### Просмотр ваших ссылок:
- В разделе "Мои ссылки" вы увидите:
  - Короткую ссылку
  - Оригинальный URL
  - Количество оставшихся переходов
  - Срок действия
  - Статус активности

#### Редактирование ссылки:
1. Найдите нужную ссылку в списке
2. Нажмите кнопку "Настройки"
3. Измените параметры:
   - Лимит переходов
   - Время жизни

#### Удаление ссылки:
1. Найдите ссылку в списке
2. Нажмите кнопку "Удалить"
3. Подтвердите удаление

### Профиль пользователя
В личном кабинете доступна информация:
- UUID пользователя
- Имя пользователя
- Email
- Общее количество ссылок
- Суммарное количество переходов
- Среднее количество переходов на ссылку


## Технические детали

### Используемые технологии
- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Thymeleaf
- PostgreSQL
- Bootstrap 5
- HTML/CSS/JavaScript

### Требования
- JDK 17 или выше
- PostgreSQL 12 или выше
- Gradle 8.0 или выше

### Архитектура проекта
Проект разделен на следующие модули:
1. Контроллеры (обработка запросов)
2. Сервисы (бизнес-логика)
3. Репозитории (работа с БД)
4. Модели данных
5. Конфигурация
6. Представления (Thymeleaf шаблоны)

### Конфигурация
Основные параметры настраиваются в файле `.env`:

```properties
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=shortlink
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password

GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

YANDEX_CLIENT_ID=your_yandex_client_id
YANDEX_CLIENT_SECRET=your_yandex_client_secret
```

### Безопасность
- CSRF защита для всех форм
- OAuth2 аутентификация
- Хеширование паролей с помощью BCrypt
- Защита от XSS-атак
- Валидация входных данных

### Особенности реализации
- Асинхронная проверка истекающих ссылок
- Адаптивный дизайн интерфейса
- Темная/светлая тема
- Real-time уведомления

## Установка и запуск

1. Клонируйте репозиторий:
```bash
git clone https://github.com/yourusername/ShortLinkSpring.git
cd ShortLinkSpring
```

2. Создайте файл `.env` в директории `src/main/resources` на основе `.env.example`:
```bash
cp src/main/resources/.env.example src/main/resources/.env
```

3. Настройте параметры в файле `.env`:
```properties
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=shortlink
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password

# Указать чтобы работало OAuth2
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
GITHUB_REDIRECT_URI=http://localhost:8080/login/oauth2/code/github

YANDEX_CLIENT_ID=your_yandex_client_id
YANDEX_CLIENT_SECRET=your_yandex_client_secret
YANDEX_REDIRECT_URI=http://localhost:8080/login/oauth2/code/yandex
```

5. Соберите проект:
```bash
./gradlew build
```

6. Запустите приложение:
```bash
./gradlew bootRun
```

Приложение будет доступно по адресу: `http://localhost:8080`

### Использование Docker

1. Соберите Docker образ:
```bash
docker build -t shortlink .
```

2. Запустите контейнеры через Docker Compose:
```bash
docker-compose up -d
```

## Лицензия

Этот проект распространяется под лицензией MIT. Подробности в файле [LICENSE](LICENSE). 