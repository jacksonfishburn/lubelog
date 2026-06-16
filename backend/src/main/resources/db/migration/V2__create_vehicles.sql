CREATE TABLE vehicles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    year        SMALLINT,
    make        VARCHAR(100),
    model       VARCHAR(100),
    trim        VARCHAR(100),
    vin         VARCHAR(17),
    nickname    VARCHAR(100),
    mileage     INT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vehicles_user_id ON vehicles(user_id);