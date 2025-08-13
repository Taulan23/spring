#!/bin/bash

echo "=== Тестовый скрипт для отладки ==="

echo "1. Очищаем базу данных"
docker-compose exec stats-db psql -U postgres -d stats_db -c "DELETE FROM endpoint_hits;"

echo "2. Проверяем GET /stats с пустой базой"
curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=false"

echo -e "\n\n3. Добавляем первый хит"
curl -X POST http://localhost:9090/hit -H "Content-Type: application/json" -d "{}"

echo -e "\n\n4. Проверяем GET /stats после первого хита"
curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=false"

echo -e "\n\n5. Добавляем второй хит"
curl -X POST http://localhost:9090/hit -H "Content-Type: application/json" -d "{}"

echo -e "\n\n6. Проверяем GET /stats после второго хита"
curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=false"

echo -e "\n\n7. Проверяем GET /stats с unique=true"
curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=true"

echo -e "\n\n8. Проверяем GET /stats без start (должен вернуть 400)"
curl -s "http://localhost:9090/stats?end=2020-05-05%2000:00:00&uris=/events" -w "\nHTTP Status: %{http_code}\n"
