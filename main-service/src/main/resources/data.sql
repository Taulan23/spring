-- Вставка тестовых данных

-- Категории
INSERT INTO categories (id, name) VALUES (1, 'Концерты') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (id, name) VALUES (2, 'Выставки') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (id, name) VALUES (3, 'Спорт') ON CONFLICT (name) DO NOTHING;

-- Пользователи
INSERT INTO users (id, name, email) VALUES (1, 'Иван Иванов', 'ivan@example.com') ON CONFLICT (email) DO NOTHING;
INSERT INTO users (id, name, email) VALUES (2, 'Мария Петрова', 'maria@example.com') ON CONFLICT (email) DO NOTHING;

-- События
INSERT INTO events (id, annotation, category_id, confirmed_requests, created_on, description, event_date, initiator_id, lat, lon, paid, participant_limit, published_on, request_moderation, state, title, views) 
VALUES (1, 'Концерт классической музыки', 1, 0, '2023-01-01 10:00:00', 'Великолепный концерт классической музыки в филармонии', '2025-06-15 19:00:00', 1, 55.7558, 37.6176, false, 100, '2023-01-01 12:00:00', true, 'PUBLISHED', 'Классический вечер', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO events (id, annotation, category_id, confirmed_requests, created_on, description, event_date, initiator_id, lat, lon, paid, participant_limit, published_on, request_moderation, state, title, views) 
VALUES (2, 'Выставка современного искусства', 2, 5, '2023-01-02 11:00:00', 'Уникальная выставка работ современных художников', '2025-07-20 14:00:00', 2, 55.7622, 37.6094, true, 50, '2023-01-02 13:00:00', false, 'PUBLISHED', 'Современное искусство', 12)
ON CONFLICT (id) DO NOTHING;

-- Создание таблицы для заявок на участие (если не существует)
CREATE TABLE IF NOT EXISTS participation_requests (
    id BIGSERIAL PRIMARY KEY,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    event_id BIGINT NOT NULL REFERENCES events(id),
    requester_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    UNIQUE(event_id, requester_id)
);

-- Создание таблицы для подборок (если не существует)
CREATE TABLE IF NOT EXISTS compilations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT false
);

-- Создание таблицы связи подборок и событий (если не существует)
CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT NOT NULL REFERENCES compilations(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);

-- Тестовые подборки
INSERT INTO compilations (id, title, pinned) VALUES (1, 'Лучшие события', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO compilations (id, title, pinned) VALUES (2, 'Культурные мероприятия', false) ON CONFLICT (id) DO NOTHING;

-- Связи подборок с событиями
INSERT INTO compilation_events (compilation_id, event_id) VALUES (1, 1) ON CONFLICT DO NOTHING;
INSERT INTO compilation_events (compilation_id, event_id) VALUES (1, 2) ON CONFLICT DO NOTHING;
INSERT INTO compilation_events (compilation_id, event_id) VALUES (2, 1) ON CONFLICT DO NOTHING;

-- Обновление последовательностей
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('events_id_seq', (SELECT MAX(id) FROM events));
SELECT setval('compilations_id_seq', (SELECT MAX(id) FROM compilations));
