-- SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: AGPL-3.0-only
CREATE TABLE
    pin_message (
        id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        request_id VARCHAR,
        pinned_by VARCHAR NOT NULL,
        room_id VARCHAR,
        luser VARCHAR,
        "server" VARCHAR,
        stanza_id VARCHAR NOT NULL,
        pinned_at TIMESTAMP DEFAULT now (),
        CONSTRAINT room_id_unique UNIQUE (room_id)
    );

CREATE INDEX i_pin_messages_stanza_id ON pin_message (stanza_id);	