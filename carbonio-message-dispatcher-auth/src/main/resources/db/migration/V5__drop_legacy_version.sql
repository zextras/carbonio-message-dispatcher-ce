-- SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: AGPL-3.0-only
-- Cleanup: remove legacy version tracking table (now managed by Flyway)

DROP TABLE IF EXISTS database_version;