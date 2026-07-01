CREATE TABLE service_reminders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_service_id  UUID NOT NULL REFERENCES vehicle_services(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sent_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_reminded_at    DATE,
    mileage_reminded_at INT,
    channel             TEXT NOT NULL DEFAULT 'EMAIL'
);

CREATE INDEX idx_service_reminders_vehicle_service_id ON service_reminders(vehicle_service_id);
