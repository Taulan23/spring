# Explore With Me

Многомодульный Spring Boot проект, состоящий из сервиса статистики и основного сервиса.

## Структура проекта

- `stats-dto` - общие DTO классы для сервиса статистики
- `stats-service` - сервис статистики (порт 9090)
- `stats-client` - HTTP клиент для работы с сервисом статистики
- `main-service` - основной сервис (порт 8080)

## Запуск проекта

### Локальный запуск

1. Убедитесь, что у вас установлены:
   - Java 17
   - Maven 3.6+
   - PostgreSQL

2. Создайте базы данных:
   ```sql
   CREATE DATABASE stats_db;
   CREATE DATABASE main_db;
   ```

3. Соберите проект:
   ```bash
   mvn clean install
   ```

4. Запустите сервисы:
   ```bash
   # Сервис статистики
   java -jar stats-service/target/stats-service-0.0.1-SNAPSHOT.jar
   
   # Основной сервис
   java -jar main-service/target/main-service-0.0.1-SNAPSHOT.jar
   ```

### Запуск через Docker

1. Соберите проект:
   ```bash
   mvn clean install
   ```

2. Запустите все сервисы:
   ```bash
   docker-compose up -d
   ```

## API Endpoints

### Сервис статистики (порт 9090)

#### Основные эндпоинты
- `POST /hit` - сохранение информации о запросе
- `GET /stats` - получение статистики

#### Системные эндпоинты
- `GET /actuator/health` - проверка состояния сервиса
- `GET /actuator/info` - информация о сервисе

### Основной сервис (порт 8081)

#### Тестовые эндпоинты
- `GET /` - главная страница
- `GET /test` - тестовый эндпоинт

#### Админские эндпоинты для категорий
- `POST /admin/categories` - создание категории
- `PATCH /admin/categories/{catId}` - обновление категории
- `DELETE /admin/categories/{catId}` - удаление категории

#### Публичные эндпоинты для категорий
- `GET /categories` - получение всех категорий
- `GET /categories/{catId}` - получение категории по ID

#### Системные эндпоинты
- `GET /actuator/health` - проверка состояния сервиса
- `GET /actuator/info` - информация о сервисе

## Тестирование API

### Сервис статистики (http://localhost:9090)

#### Сохранение статистики
```bash
curl -X POST http://localhost:9090/hit \
  -H "Content-Type: application/json" \
  -d '{
    "app": "ewm-main-service",
    "uri": "/events/1",
    "ip": "192.168.1.1",
    "timestamp": "2024-01-01T12:00:00"
  }'
```

#### Получение статистики
```bash
# Все запросы за период
curl "http://localhost:9090/stats?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59"

# Фильтрация по URI
curl "http://localhost:9090/stats?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59&uris=/events/1&unique=false"

# Уникальные посещения
curl "http://localhost:9090/stats?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59&unique=true"
```

#### Проверка состояния
```bash
curl http://localhost:9090/actuator/health
```

### Основной сервис (http://localhost:8081)

#### Главная страница
```bash
curl http://localhost:8081/
```

#### Тестовый эндпоинт
```bash
curl http://localhost:8081/test
```

#### Создание категории
```bash
curl -X POST http://localhost:8081/admin/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Концерты"}'
```

#### Получение всех категорий
```bash
curl http://localhost:8081/categories
```

#### Получение категории по ID
```bash
curl http://localhost:8081/categories/1
```

#### Обновление категории
```bash
curl -X PATCH http://localhost:8081/admin/categories/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Обновленная категория"}'
```

#### Удаление категории
```bash
curl -X DELETE http://localhost:8081/admin/categories/1
```

#### Проверка состояния
```bash
curl http://localhost:8081/actuator/health
```

### Примеры ответов

#### Успешное создание категории
```json
{
  "id": 1,
  "name": "Концерты"
}
```

#### Список категорий
```json
[
  {
    "id": 1,
    "name": "Концерты"
  },
  {
    "id": 2,
    "name": "Выставки"
  }
]
```

#### Статистика
```json
[
  {
    "app": "ewm-main-service",
    "uri": "/events/1",
    "hits": 5
  }
]
```

#### Health check
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

## Технологии

- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Docker
- Maven
- Lombok
