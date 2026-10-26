-- Таблица кредитных договоров
CREATE TABLE IF NOT EXISTS credit_agreements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    iin VARCHAR(12) NOT NULL,
    product_type VARCHAR(32) NOT NULL,
    approved_amount NUMERIC(12, 2) NOT NULL,
    term_months INT NOT NULL,
    interest_rate NUMERIC(5, 2) NOT NULL,
    monthly_payment NUMERIC(12, 2) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблица помесячных записей графика платежей
CREATE TABLE IF NOT EXISTS payment_schedule_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agreement_id UUID NOT NULL REFERENCES credit_agreements(id) ON DELETE CASCADE,
    payment_number INT NOT NULL,
    payment_date DATE NOT NULL,
    monthly_payment NUMERIC(12, 2) NOT NULL,
    principal_payment NUMERIC(12, 2) NOT NULL,
    interest_payment NUMERIC(12, 2) NOT NULL,
    remaining_debt NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для ускорения поиска по ИИН и внешним ключам
CREATE INDEX IF NOT EXISTS idx_agreements_iin ON credit_agreements(iin);
CREATE INDEX IF NOT EXISTS idx_agreements_app_id ON credit_agreements(application_id);
CREATE INDEX IF NOT EXISTS idx_schedule_agreement_id ON payment_schedule_items(agreement_id);
CREATE INDEX IF NOT EXISTS idx_schedule_agreement_num ON payment_schedule_items(agreement_id, payment_number);

-- Таблица для паттерна Transactional Outbox (гарантированная доставка событий)
CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    topic VARCHAR(128) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_events(status, created_at);