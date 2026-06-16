// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.dal.impl;

import static com.zextras.carbonio.message.dispatcher.auth.PostgresTestSupport.dataSourceFor;
import static com.zextras.carbonio.message.dispatcher.auth.PostgresTestSupport.newContainer;
import static com.zextras.carbonio.message.dispatcher.auth.PostgresTestSupport.tableExists;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DatabaseMigrationIT {

  private static final List<String> EXPECTED_TABLES =
      List.of("users", "fast_auth_token", "pin_message", "blocklist", "discovery_nodes", "caps");

  @Container static PostgreSQLContainer<?> postgres = newContainer();

  private static DataSource dataSource;
  private static DatabaseManagerFlyway databaseManager;

  @BeforeAll
  static void setUp() {
    dataSource = dataSourceFor(postgres);
    databaseManager = new DatabaseManagerFlyway(dataSource);
    databaseManager.initialize();
  }

  @Test
  void freshInstallRunsAllMigrations() {
    assertEquals("7", databaseManager.getDatabaseVersion());
    assertTrue(databaseManager.isDatabaseLive());
    assertTrue(databaseManager.isDatabaseCorrectVersion());
  }

  @Test
  void allExpectedTablesExist() throws Exception {
    for (String table : EXPECTED_TABLES) {
      assertTrue(tableExists(dataSource, table), "Missing table: " + table);
    }
  }

  @Test
  void legacyVersionTableIsDropped() throws Exception {
    assertFalse(
        tableExists(dataSource, "database_version"),
        "database_version should have been dropped by V5");
  }

  @Test
  void legacyBaselineMigrationFrom621() throws Exception {
    try (PostgreSQLContainer<?> legacy = newContainer()) {
      legacy.start();
      DataSource legacyDataSource = dataSourceFor(legacy);

      try (Connection conn = legacyDataSource.getConnection();
          Statement stmt = conn.createStatement()) {
        stmt.execute("CREATE TABLE database_version (version VARCHAR(16) NOT NULL PRIMARY KEY)");
        stmt.execute("INSERT INTO database_version VALUES ('6.2.1')");
      }

      DatabaseManagerFlyway legacyManager = new DatabaseManagerFlyway(legacyDataSource);
      legacyManager.initialize();

      assertEquals("7", legacyManager.getDatabaseVersion());
      assertTrue(legacyManager.isDatabaseCorrectVersion());

      for (String table : List.of("fast_auth_token", "pin_message", "blocklist")) {
        assertTrue(
            tableExists(legacyDataSource, table), "Missing table after legacy migration: " + table);
      }
      assertFalse(
          tableExists(legacyDataSource, "database_version"),
          "database_version should have been dropped");
    }
  }
}
