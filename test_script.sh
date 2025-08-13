#!/bin/bash

echo "1. Очищаем базу данных"
docker-compose exec stats-db psql -U postgres -d stats_db -c "DELETE FROM endpoint_hits;"

echo "2. Проверяем GET /stats с пустой базой"
curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=false"

echo -e "

3. Добавляем первый хит"
curl -X POST http://localhost:9090/hit -H "Content-Type: application/json" -d "{}"

echo -e "

4. Проверяем GET /stats после первого хита"
curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=false"

echo -e "

5. Добавляем второй хит"
curl -X POST http://localhost:9090/hit -H "Content-Type: application/json" -d "{}"

echo -e "

6. Проверяем GET /stats после второго хита"
curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=false"

echo -e "

7. Проверяем GET /stats с unique=true"
curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=true"

