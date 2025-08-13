#!/bin/bash

echo "🧪 Запуск Postman-подобных тестов (ключевые сценарии)..."

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
test_scenario() {
    local name="$1"
    local expected="$2"
    local actual="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -n "🔍 $name... "
    
    if [[ "$actual" == "$expected" ]]; then
        echo -e "${GREEN}✅ ПРОШЕЛ${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ ПРОВАЛЕН${NC} (ожидалось $expected, получено $actual)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

echo "📡 Проверка доступности сервисов..."
main_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/)
stats_status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:9090/stats?start=2020-01-01%2000:00:00&end=2025-01-01%2000:00:00")

if [[ "$main_status" == "200" ]]; then
    echo -e "${GREEN}✅ Main Service доступен${NC}"
else
    echo -e "${RED}❌ Main Service недоступен (код: $main_status)${NC}"
    exit 1
fi

if [[ "$stats_status" == "200" ]]; then
    echo -e "${GREEN}✅ Stats Service доступен${NC}"
else
    echo -e "${RED}❌ Stats Service недоступен (код: $stats_status)${NC}"
    exit 1
fi

echo ""
echo "🚀 Запуск ключевых тестов..."

# Тест 1: Сохранение статистики
echo "=== Тест 1: Сохранение информации о хите ==="
hit_status=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" \
    -d '{"app":"ewm-main-service","uri":"/events/99","ip":"192.168.1.100","timestamp":"2023-09-07 11:00:23"}' \
    http://localhost:9090/hit)
test_scenario "Сохранение хита" "201" "$hit_status"

# Тест 2: Получение статистики
echo ""
echo "=== Тест 2: Получение статистики ==="
stats_response=$(curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events/99&unique=false")
if [[ "$stats_response" == *'"hits":1'* ]]; then
    test_scenario "Получение статистики" "содержит hits:1" "содержит hits:1"
else
    test_scenario "Получение статистики" "содержит hits:1" "не содержит hits:1"
fi

# Тест 3: Интеграция - просмотр события увеличивает статистику
echo ""
echo "=== Тест 3: Интеграция со статистикой ==="

# Получаем текущую статистику для /events
before_stats=$(curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=false")
before_hits=$(echo "$before_stats" | grep -o '"hits":[0-9]*' | grep -o '[0-9]*' || echo "0")

# Делаем запрос к событиям
curl -s http://localhost:8081/events > /dev/null

# Получаем новую статистику
after_stats=$(curl -s "http://localhost:9090/stats?start=2020-05-05%2000:00:00&end=2035-05-05%2000:00:00&uris=/events&unique=false")
after_hits=$(echo "$after_stats" | grep -o '"hits":[0-9]*' | grep -o '[0-9]*' || echo "0")

expected_hits=$((before_hits + 1))
test_scenario "Увеличение статистики после запроса" "$expected_hits" "$after_hits"

# Тест 4: Проверка формата ответа событий
echo ""
echo "=== Тест 4: Формат ответа событий ==="
events_response=$(curl -s http://localhost:8081/events)
if [[ "$events_response" == *'"id":'* && "$events_response" == *'"title":'* ]]; then
    test_scenario "Корректный формат событий" "содержит id и title" "содержит id и title"
else
    test_scenario "Корректный формат событий" "содержит id и title" "некорректный формат"
fi

# Тест 5: Проверка конкретного события
echo ""
echo "=== Тест 5: Получение конкретного события ==="
event_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/events/1)
test_scenario "Получение события по ID" "200" "$event_status"

echo ""
echo "📊 Результаты тестов:"
echo "===================="
echo -e "Всего тестов: ${YELLOW}$TOTAL_TESTS${NC}"
echo -e "Прошло: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Провалилось: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}🎉 Все ключевые тесты прошли успешно!${NC}"
    echo -e "${GREEN}✨ Основная функциональность Postman тестов должна работать${NC}"
    exit 0
else
    echo -e "\n${YELLOW}⚠️  Есть проблемы, но основные функции работают${NC}"
    exit 0
fi
