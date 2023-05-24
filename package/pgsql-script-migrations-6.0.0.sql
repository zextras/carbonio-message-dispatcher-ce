-- SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: AGPL-3.0-only

CREATE TABLE database_version
(
    version VARCHAR(16) NOT NULL,
    PRIMARY KEY (version)
);
INSERT INTO database_version (version) VALUES ('6.0.0');