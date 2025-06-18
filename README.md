![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

<!-- этот файл можно и нужно менять -->

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:
* Bot
* Scrapper

Для работы требуется БД `PostgreSQL`. Присутствует опциональная зависимость на `Kafka`.

Для дополнительной справки: [HELP.md](./HELP.md)

## Сборка и запуск

1. Собрать проект:

   ```shell
   mvn clean package
   ```
2. Заполнить конфигурационные файлы `config.env` в папках `bot` и `scrapper`.

```
bot/config.env
TELEGRAM_TOKEN=ТОКЕН_ВАШЕГО_ТЕЛЕГРАМ_БОТА

scrapper/config.env
GITHUB_TOKEN=ТОКЕН_GITHUB
SO_TOKEN_KEY=КЛЮЧ_STACKOVERFLOW
```

3. Выполнить команду:

   ```shell
   docker-compose --profile full up
   ```
4. Приложение готово к работе, можно переходить в телеграм бота.

## Архитектура

<img width="964" alt="Снимок экрана 2025-06-18 в 14 04 26" src="https://github.com/user-attachments/assets/aa093c15-ec84-4076-a25f-50d7994eb46e" />

## Контакты

```
Разработал: Дивиров Арсен

Мои контакты: 
Telegram - @kiz_zyaka
Почта - ArsenDivirov@yandex.ru
```

