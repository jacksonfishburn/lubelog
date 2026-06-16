CREATE TABLE services (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    is_global   BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_services_global_no_user
        CHECK (is_global = false OR user_id IS NULL)
);

CREATE INDEX idx_services_user_id ON services(user_id);