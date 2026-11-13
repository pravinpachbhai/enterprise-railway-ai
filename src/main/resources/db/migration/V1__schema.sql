CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE trains (
  id BIGSERIAL PRIMARY KEY,
  train_id VARCHAR(80) UNIQUE NOT NULL,
  status VARCHAR(40) NOT NULL,
  current_location VARCHAR(200) NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE incidents (
  id BIGSERIAL PRIMARY KEY,
  train_id VARCHAR(80) NOT NULL REFERENCES trains(train_id),
  delay_minutes INTEGER NOT NULL CHECK (delay_minutes >= 0),
  cause VARCHAR(100) NOT NULL,
  description TEXT NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE notification_workflows (
  id BIGSERIAL PRIMARY KEY,
  incident_id BIGINT NOT NULL REFERENCES incidents(id),
  state VARCHAR(40) NOT NULL,
  plan_json JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY,
  workflow_id BIGINT NOT NULL REFERENCES notification_workflows(id),
  recipient VARCHAR(200) NOT NULL,
  message TEXT NOT NULL,
  idempotency_key VARCHAR(200) NOT NULL UNIQUE,
  status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
  provider_reference VARCHAR(200),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_events (
  id BIGSERIAL PRIMARY KEY,
  workflow_id BIGINT,
  actor VARCHAR(200) NOT NULL,
  action VARCHAR(100) NOT NULL,
  details JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO trains(train_id,status,current_location) VALUES
 ('BNSF-7821','DELAYED','Milepost 42 - fictional subdivision'),
 ('BNSF-4410','ON_TIME','Yard North - fictional subdivision'),
 ('BNSF-3320','ON_TIME','Terminal West - fictional subdivision')
ON CONFLICT (train_id) DO NOTHING;
