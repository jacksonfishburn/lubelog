CREATE TABLE vehicle_services (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id      UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    service_id      UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    interval_miles  INT,
    interval_months SMALLINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_vehicle_services UNIQUE (vehicle_id, service_id),
    CONSTRAINT chk_vehicle_services_interval
        CHECK (interval_miles IS NOT NULL OR interval_months IS NOT NULL)
);

CREATE INDEX idx_vehicle_services_vehicle_id ON vehicle_services(vehicle_id);