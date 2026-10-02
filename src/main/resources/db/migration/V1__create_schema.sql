CREATE TABLE rooms (
    id       VARCHAR(40)  PRIMARY KEY,
    name     VARCHAR(120) NOT NULL,
    capacity INT          NOT NULL CHECK (capacity > 0)
);

CREATE TABLE reservations (
    id        UUID         PRIMARY KEY,
    room_id   VARCHAR(40)  NOT NULL REFERENCES rooms (id),
    organizer VARCHAR(120) NOT NULL,
    starts_at TIMESTAMPTZ  NOT NULL,
    ends_at   TIMESTAMPTZ  NOT NULL,
    attendees INT          NOT NULL CHECK (attendees > 0),
    status    VARCHAR(20)  NOT NULL,
    CONSTRAINT reservation_ends_after_start CHECK (ends_at > starts_at)
);

-- The overlap query filters by room and by window, in that order.
CREATE INDEX idx_reservations_room_window ON reservations (room_id, starts_at, ends_at);

INSERT INTO rooms (id, name, capacity) VALUES
    ('AQUARIUM',    'Aquarium',    6),
    ('ATRIUM',      'Atrium',      20),
    ('BOARDROOM',   'Boardroom',   12),
    ('PHONE_BOOTH', 'Phone booth', 2);
