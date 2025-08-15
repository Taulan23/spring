#!/bin/bash

echo "🧪 Запуск Postman-подобных тестов..."

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Счетчики
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Функция для тестирования
test_endpoint() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_status="$5"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -n "🔍 Тест: $name... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json "$url")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X POST -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" = "PATCH" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X PATCH -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X DELETE "$url")
    fi
    
    http_code="${response: -3}"
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✅ ПРОШЕЛ${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ ПРОВАЛЕН${NC} (ожидался $expected_status, получен $http_code)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        if [ -f /tmp/response.json ]; then
            echo "   Ответ: $(cat /tmp/response.json)"
        fi
    fi
}

# Проверяем, что сервисы запущены
echo "📡 Проверка доступности сервисов..."

if curl -s http://localhost:8081/ > /dev/null; then
    echo -e "${GREEN}✅ Main Service доступен${NC}"
else
    echo -e "${RED}❌ Main Service недоступен${NC}"
    echo "Запустите сервисы: docker-compose up -d"
    exit 1
fi

if curl -s http://localhost:9090/stats?start=2020-01-01%2000:00:00&end=2025-01-01%2000:00:00 > /dev/null; then
    echo -e "${GREEN}✅ Stats Service доступен${NC}"
else
    echo -e "${YELLOW}⚠️  Stats Service недоступен (пропускаем тесты статистики)${NC}"
fi

echo ""
echo "🚀 Запуск тестов Main Service..."

# Тесты Main Service
test_endpoint "Главная страница" "GET" "http://localhost:8081/" "" "200"
test_endpoint "Тестовая страница" "GET" "http://localhost:8081/test" "" "200"
test_endpoint "Health Check" "GET" "http://localhost:8081/actuator/health" "" "200"

# Тесты категорий
test_endpoint "Создание категории" "POST" "http://localhost:8081/admin/categories" '{"name": "НоваяКатегория'$(date +%s)'"}' "201"
test_endpoint "Создание категории (дубликат)" "POST" "http://localhost:8081/admin/categories" '{"name": "Концерты"}' "409"
test_endpoint "Создание категории (пустое имя)" "POST" "http://localhost:8081/admin/categories" '{"name": ""}' "400"

test_endpoint "Получение всех категорий" "GET" "http://localhost:8081/categories" "" "200"
test_endpoint "Получение категории по ID" "GET" "http://localhost:8081/categories/1" "" "200"
test_endpoint "Получение несуществующей категории" "GET" "http://localhost:8081/categories/999999" "" "404"

test_endpoint "Обновление категории" "PATCH" "http://localhost:8081/admin/categories/1" '{"name": "Обновленные концерты"}' "200"
test_endpoint "Обновление несуществующей категории" "PATCH" "http://localhost:8081/admin/categories/999999" '{"name": "Несуществующая"}' "404"

# Создаем категорию и получаем ее ID из ответа
TIMESTAMP=$(date +%s)
CATEGORY_NAME="ТестДляУдаления$TIMESTAMP"
echo -n "🔍 Тест: Создание категории для удаления... "
CATEGORY_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/category_response.json -X POST -H "Content-Type: application/json" -d "{\"name\": \"$CATEGORY_NAME\"}" "http://localhost:8081/admin/categories")
CATEGORY_HTTP_CODE="${CATEGORY_RESPONSE: -3}"
if [ "$CATEGORY_HTTP_CODE" = "201" ]; then
    echo -e "${GREEN}✅ ПРОШЕЛ${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
    CATEGORY_TO_DELETE=$(cat /tmp/category_response.json | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$CATEGORY_TO_DELETE" ]; then
        test_endpoint "Удаление категории" "DELETE" "http://localhost:8081/admin/categories/$CATEGORY_TO_DELETE" "" "204"
    else
        echo -e "${RED}❌ ПРОВАЛЕН${NC} (не удалось получить ID категории)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
    fi
else
    echo -e "${RED}❌ ПРОВАЛЕН${NC} (ожидался 201, получен $CATEGORY_HTTP_CODE)"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))
test_endpoint "Удаление несуществующей категории" "DELETE" "http://localhost:8081/admin/categories/999999" "" "404"

echo ""
echo "📊 Результаты тестов:"
echo "===================="
echo -e "Всего тестов: ${YELLOW}$TOTAL_TESTS${NC}"
echo -e "Прошло: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Провалилось: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}🎉 Все тесты прошли успешно!${NC}"
    exit 0
else
    echo -e "\n${RED}❌ Некоторые тесты провалились${NC}"
    exit 1
fi
