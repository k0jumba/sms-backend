# SMS Backend

## Предварительная настройка

Создать `.env.scratch` и `.env.prod`. Заполнить их, согласно .env.scratch.example и .env.prod.example.

## Запустить тесты

Удалить старые сборки

`./mvnw clean`

Запустить тесты

`./mvnw test`

## Запустить scratch

Удалить старые сборки

`./mvnw clean`

Загрузить переменные окружения scratch-конфига

`./load-env.ps1 .env.scratch`

Запустить одноразовую БД в контейнере в фоновом процессе

`docker compose -f docker-compose.scratch.yml up -d`

Собрать и запустить сервер без проведения тестов

`./mvnw spring-boot:run`

## Запустить prod

Загрузить переменные окружения prod-конфига

`./load-env.ps1 .env.prod`

Запустить сервер в контейнере в фоновом процессе

`docker compose -f docker-compose.prod.yml up -d `