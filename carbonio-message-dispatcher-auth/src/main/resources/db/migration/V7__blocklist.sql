-- SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: AGPL-3.0-only
CREATE TABLE blocklist
(
    luser   VARCHAR(250) NOT NULL,
    lserver VARCHAR(250) NOT NULL,
    reason  TEXT,
    PRIMARY KEY (luser, lserver)
);
