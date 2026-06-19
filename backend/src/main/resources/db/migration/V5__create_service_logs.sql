CREATE TABLE service_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_service_id  UUID NOT NULL REFERENCES vehicle_services(id) ON DELETE CASCADE,
    done_at_mileage     INT,
    done_at_date        DATE NOT NULL,
    cost                NUMERIC(10, 2),
    notes               TEXT,
created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_service_logs_vehicle_service_id ON service_logs(vehicle_service_id);