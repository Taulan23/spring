#!/bin/bash

echo "🧪 ПОШАГОВОЕ ТЕСТИРОВАНИЕ POSTMAN КОЛЛЕКЦИИ"
echo "============================================="

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
NC='\033[0m'

# Базовые URL
BASE_URL="http://localhost:8081"
STATS_URL="http://localhost:9090"

# Счетчики
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Функция для тестирования
test_request() {
    local test_name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_code="$5"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo ""
    echo -e "${BLUE}Тест $TOTAL_TESTS: $test_name${NC}"
    echo "Запрос: $method $url"
    
    if [ "$method" = "POST" ] && [ -n "$data" ]; then
        response=$(curl -s -w "HTTPCODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "HTTPCODE:%{http_code}" -X POST "$url")
    else
        response=$(curl -s -w "HTTPCODE:%{http_code}" "$url")
    fi
    
    # Извлекаем HTTP код
    http_code=$(echo "$response" | grep -o "HTTPCODE:[0-9]*" | cut -d: -f2)
    body=$(echo "$response" | sed 's/HTTPCODE:[0-9]*$//')
    
    echo "Ответ: HTTP $http_code"
    
    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✅ ПРОШЕЛ${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ ПРОВАЛЕН${NC} (ожидался $expected_code)"
        echo "Тело ответа: $body"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

echo -e "${YELLOW}Проверка базовой доступности сервисов...${NC}"

# Проверка доступности
main_status=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/")
stats_status=$(curl -s -o /dev/null -w "%{http_code}" "$STATS_URL/stats?start=2020-01-01%2000:00:00&end=2025-01-01%2000:00:00")

if [ "$main_status" != "200" ]; then
    echo -e "${RED}❌ Main Service недоступен (HTTP $main_status)${NC}"
    exit 1
fi

if [ "$stats_status" != "200" ]; then
    echo -e "${RED}❌ Stats Service недоступен (HTTP $stats_status)${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Оба сервиса доступны${NC}"

echo ""
echo -e "${YELLOW}=== ИМИТАЦИЯ ПЕРВЫХ 20 POSTMAN ТЕСТОВ ===${NC}"

# Создаем тестового пользователя для использования в тестах
echo "Создание тестового пользователя..."
unique_email="testuser_$(date +%s)@test.com"
user_response=$(curl -s -H "Content-Type: application/json" -d "{\"name\":\"Test User\",\"email\":\"$unique_email\"}" "$BASE_URL/admin/users")
user_id=$(echo "$user_response" | grep -o '"id":[0-9]*' | cut -d: -f2 || echo "1")
echo "Создан пользователь с ID: $user_id"

# Создаем тестовую категорию
echo "Создание тестовой категории..."
unique_cat_name="TestCategory_$(date +%s)"
cat_response=$(curl -s -H "Content-Type: application/json" -d "{\"name\":\"$unique_cat_name\"}" "$BASE_URL/admin/categories")
cat_id=$(echo "$cat_response" | grep -o '"id":[0-9]*' | cut -d: -f2 || echo "1")
echo "Создана категория с ID: $cat_id"

# ТЕСТ 1: Validation - Добавление запроса без eventId (должен быть 400)
test_request "Добавление запроса без eventId" "POST" "$BASE_URL/users/$user_id/requests" "" "400"

# ТЕСТ 2: Получение событий пользователя (должен быть 200)
test_request "Получение событий пользователя" "GET" "$BASE_URL/users/$user_id/events" "" "200"

# ТЕСТ 3: Получение публичных событий (должен быть 200)
test_request "Получение публичных событий" "GET" "$BASE_URL/events" "" "200"

# ТЕСТ 4: Получение публичных категорий (должен быть 200)
test_request "Получение публичных категорий" "GET" "$BASE_URL/categories" "" "200"

# ТЕСТ 5: Получение админских пользователей (должен быть 200)
test_request "Получение админских пользователей" "GET" "$BASE_URL/admin/users" "" "200"

# ТЕСТ 6: Получение админских категорий (должен быть 200)
test_request "Получение админских категорий" "GET" "$BASE_URL/admin/categories" "" "200"

# ТЕСТ 7: Создание события без description (должен быть 400)
event_data_no_desc='{"annotation":"Test event annotation with more than 20 characters","category":'$cat_id',"eventDate":"2025-12-31 15:00:00","location":{"lat":55.754167,"lon":37.62},"paid":false,"participantLimit":10,"requestModeration":true,"title":"Test Event"}'
test_request "Создание события без description" "POST" "$BASE_URL/users/$user_id/events" "$event_data_no_desc" "400"

# ТЕСТ 8: Создание события с пустым description (должен быть 400)
event_data_empty_desc='{"annotation":"Test event annotation with more than 20 characters","category":'$cat_id',"description":"","eventDate":"2025-12-31 15:00:00","location":{"lat":55.754167,"lon":37.62},"paid":false,"participantLimit":10,"requestModeration":true,"title":"Test Event"}'
test_request "Создание события с пустым description" "POST" "$BASE_URL/users/$user_id/events" "$event_data_empty_desc" "400"

# ТЕСТ 9: Создание валидного события (должен быть 201)
event_data_valid='{"annotation":"Test event annotation with more than 20 characters","category":'$cat_id',"description":"Test event description with more than 20 characters","eventDate":"2025-12-31 15:00:00","location":{"lat":55.754167,"lon":37.62},"paid":false,"participantLimit":10,"requestModeration":true,"title":"Test Event"}'
event_response=$(curl -s -w "HTTPCODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$event_data_valid" "$BASE_URL/users/$user_id/events")
event_http_code=$(echo "$event_response" | grep -o "HTTPCODE:[0-9]*" | cut -d: -f2)
event_body=$(echo "$event_response" | sed 's/HTTPCODE:[0-9]*$//')
event_id=$(echo "$event_body" | grep -o '"id":[0-9]*' | cut -d: -f2 || echo "1")

echo ""
echo -e "${BLUE}Тест 9: Создание валидного события${NC}"
echo "Запрос: POST $BASE_URL/users/$user_id/events"
echo "Ответ: HTTP $event_http_code"

if [ "$event_http_code" = "201" ]; then
    echo -e "${GREEN}✅ ПРОШЕЛ${NC}"
    echo "Создано событие с ID: $event_id"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}❌ ПРОВАЛЕН${NC} (ожидался 201)"
    echo "Тело ответа: $event_body"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))

# ТЕСТ 10: Получение созданного события (должен быть 200)
test_request "Получение созданного события" "GET" "$BASE_URL/users/$user_id/events/$event_id" "" "200"

# ТЕСТ 11: Создание заявки на участие в событии (должен быть 201 или 400 если свое событие)
test_request "Создание заявки на участие" "POST" "$BASE_URL/users/$user_id/requests?eventId=$event_id" "" "400"

# ТЕСТ 12: Получение заявок пользователя (должен быть 200)
test_request "Получение заявок пользователя" "GET" "$BASE_URL/users/$user_id/requests" "" "200"

# ТЕСТ 13: Фильтрация событий по категории (должен быть 200)
test_request "Фильтрация событий по категории" "GET" "$BASE_URL/events?categories=$cat_id" "" "200"

# ТЕСТ 14: Статистика - проверка интеграции (должен быть 200)
test_request "Получение статистики" "GET" "$STATS_URL/stats?start=2020-01-01%2000:00:00&end=2025-01-01%2000:00:00" "" "200"

# ТЕСТ 15: Отправка статистики хита (должен быть 201)
hit_data='{"app":"ewm-main-service","uri":"/test","ip":"127.0.0.1","timestamp":"2023-09-07 11:00:23"}'
test_request "Отправка статистики хита" "POST" "$STATS_URL/hit" "$hit_data" "201"

echo ""
echo "📊 ИТОГОВЫЕ РЕЗУЛЬТАТЫ ТЕРМИНАЛЬНОГО ТЕСТИРОВАНИЯ:"
echo "================================================="
echo -e "Всего тестов: ${YELLOW}$TOTAL_TESTS${NC}"
echo -e "Прошло: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Провалилось: ${RED}$FAILED_TESTS${NC}"

success_rate=$(echo "scale=1; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc -l 2>/dev/null || echo "$(($PASSED_TESTS * 100 / $TOTAL_TESTS))")
echo -e "Процент успеха: ${YELLOW}$success_rate%${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ВСЕ ТЕРМИНАЛЬНЫЕ ТЕСТЫ ПРОШЛИ!${NC}"
    echo -e "${GREEN}✨ Проблема не в API, а в Postman коллекции${NC}"
elif [ $FAILED_TESTS -lt 3 ]; then
    echo -e "\n${YELLOW}⚠️  Большинство тестов работает${NC}"
    echo -e "${YELLOW}🔍 Проблема может быть в специфических Postman скриптах${NC}"
else
    echo -e "\n${RED}❌ Есть серьезные проблемы с API${NC}"
    echo -e "${RED}🔧 Нужна дополнительная диагностика${NC}"
fi

echo ""
echo -e "${BLUE}🔍 СЛЕДУЮЩИЕ ШАГИ:${NC}"
echo "1. Если терминальные тесты проходят, проблема в Postman скриптах"
echo "2. Если нет - нужно исправлять API"
echo "3. Проверить логи сервисов: docker-compose logs main-service"
