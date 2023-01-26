-- SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: AGPL-3.0-only

CREATE TABLE chats_messaging_db_version
(
    version VARCHAR(16) NOT NULL,
    PRIMARY KEY (version)
);
INSERT INTO chats_messaging_db_version (version) VALUES ('5.1.0');