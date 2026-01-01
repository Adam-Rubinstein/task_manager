-- ============================================================================
-- Voice Task Manager - SQL Schema для PostgreSQL
-- Инициализация базы данных
-- ============================================================================

-- Таблица задач
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date TIMESTAMP NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    priority INTEGER NOT NULL DEFAULT 0 CHECK (priority >= 0 AND priority <= 10),
    recurrence_type VARCHAR(50) NOT NULL DEFAULT 'ONCE',
    recurrence_interval INTEGER,
    recurrence_day_of_week INTEGER CHECK (recurrence_day_of_week IS NULL OR (recurrence_day_of_week >= 1 AND recurrence_day_of_week <= 7)),
    recurrence_day_of_month INTEGER CHECK (recurrence_day_of_month IS NULL OR (recurrence_day_of_month >= 1 AND recurrence_day_of_month <= 31)),
    recurrence_end_date TIMESTAMP,
    recurrence_count INTEGER,
    last_completed_date TIMESTAMP,
    recurrence_parent_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Индексы для поиска и фильтрации
CREATE INDEX IF NOT EXISTS idx_due_date ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_priority ON tasks(priority);
CREATE INDEX IF NOT EXISTS idx_recurrence_parent ON tasks(recurrence_parent_id);
CREATE INDEX IF NOT EXISTS idx_created_at ON tasks(created_at);

-- Таблица тегов для задач
CREATE TABLE IF NOT EXISTS task_tags (
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (task_id, tag)
);

-- Таблица алертов/уведомлений
CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    message VARCHAR(500) NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alert_task_id ON alerts(task_id);
CREATE INDEX IF NOT EXISTS idx_alert_read ON alerts(is_read);

-- Таблица аудиофайлов (хранятся 30 дней)
CREATE TABLE IF NOT EXISTS audio_files (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL UNIQUE REFERENCES tasks(id) ON DELETE CASCADE,
    audio_data BYTEA NOT NULL,
    file_size BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP + INTERVAL '30 days'
);

CREATE INDEX IF NOT EXISTS idx_audio_expires_at ON audio_files(expires_at);
CREATE INDEX IF NOT EXISTS idx_audio_task_id ON audio_files(task_id);

-- ============================================================================
-- Функции и триггеры для автоматического обновления updated_at
-- ============================================================================

CREATE OR REPLACE FUNCTION update_task_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS task_update_timestamp ON tasks;

CREATE TRIGGER task_update_timestamp
BEFORE UPDATE ON tasks
FOR EACH ROW
EXECUTE FUNCTION update_task_updated_at();

-- ============================================================================
-- Функция для автоматического удаления истекших аудиофайлов
-- ============================================================================

CREATE OR REPLACE FUNCTION delete_expired_audio_files()
RETURNS void AS $$
BEGIN
    DELETE FROM audio_files
    WHERE expires_at < CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;
