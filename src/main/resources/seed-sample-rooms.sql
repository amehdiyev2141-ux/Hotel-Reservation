BEGIN;

LOCK TABLE room_entity IN EXCLUSIVE MODE;

INSERT INTO room_entity (number, type, price_per_night)
SELECT sample.number, sample.type, sample.price_per_night
FROM (VALUES
    (102, 'SINGLE', 95.00::numeric),
    (103, 'DOUBLE', 120.00::numeric),
    (104, 'SUITE', 200.00::numeric),
    (105, 'SINGLE', 100.00::numeric)
) AS sample(number, type, price_per_night)
WHERE NOT EXISTS (
    SELECT 1
    FROM room_entity AS existing
    WHERE existing.number = sample.number
);

COMMIT;
