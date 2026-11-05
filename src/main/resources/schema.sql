ALTER TABLE IF EXISTS reservation_entity
    DROP CONSTRAINT IF EXISTS reservation_entity_status_check;

UPDATE reservation_entity
SET status = 'CANCELLED'
WHERE status = 'CANCELED';

ALTER TABLE IF EXISTS reservation_entity
    ADD CONSTRAINT reservation_entity_status_check
    CHECK (status IN ('ACTIVE', 'CANCELLED'));
