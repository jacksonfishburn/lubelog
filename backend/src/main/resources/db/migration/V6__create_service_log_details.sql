CREATE TABLE service_log_details (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_log_id  UUID NOT NULL REFERENCES service_logs(id) ON DELETE CASCADE,
    key             VARCHAR(100) NOT NULL,
    value           TEXT NOT NULL,

    CONSTRAINT uq_service_log_details UNIQUE (service_log_id, key)
);

CREATE INDEX idx_service_log_details_log_id ON service_log_details(service_log_id);