// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.dal.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DatabaseMigrationIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

  private static PGSimpleDataSource dataSource;
  private static DatabaseManagerFlyway databaseManager;

  @BeforeAll
  static void setUp() {
    dataSource = buildDataSource(postgres);
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
    try (Connection conn = dataSource.getConnection()) {
      DatabaseMetaData meta = conn.getMetaData();
      for (String table :
          List.of(
              "users", "fast_auth_token", "pin_message", "blocklist", "discovery_nodes", "caps")) {
        try (ResultSet rs = meta.getTables(null, null, table, new String[] {"TABLE"})) {
          assertTrue(rs.next(), "Missing table: " + table);
        }
      }
    }
  }

  @Test
  void legacyVersionTableIsDropped() throws Exception {
    try (Connection conn = dataSource.getConnection()) {
      DatabaseMetaData meta = conn.getMetaData();
      try (ResultSet rs = meta.getTables(null, null, "database_version", new String[] {"TABLE"})) {
        assertFalse(rs.next(), "database_version should have been dropped by V5");
      }
    }
  }

  @Test
  void legacyBaselineMigrationFrom621() throws Exception {
    try (PostgreSQLContainer<?> legacy = new PostgreSQLContainer<>("postgres:15-alpine")) {
      legacy.start();

      PGSimpleDataSource legacyDataSource = buildDataSource(legacy);

      try (Connection conn = legacyDataSource.getConnection();
          Statement stmt = conn.createStatement()) {
        stmt.execute("CREATE TABLE database_version (version VARCHAR(16) NOT NULL PRIMARY KEY)");
        stmt.execute("INSERT INTO database_version VALUES ('6.2.1')");
      }

      DatabaseManagerFlyway legacyManager = new DatabaseManagerFlyway(legacyDataSource);
      legacyManager.initialize();

      assertEquals("7", legacyManager.getDatabaseVersion());
      assertTrue(legacyManager.isDatabaseCorrectVersion());

      try (Connection conn = legacyDataSource.getConnection()) {
        DatabaseMetaData meta = conn.getMetaData();
        for (String table : List.of("fast_auth_token", "pin_message", "blocklist")) {
          try (ResultSet rs = meta.getTables(null, null, table, new String[] {"TABLE"})) {
            assertTrue(rs.next(), "Missing table after legacy migration: " + table);
          }
        }
        try (ResultSet rs =
            meta.getTables(null, null, "database_version", new String[] {"TABLE"})) {
          assertFalse(rs.next(), "database_version should have been dropped");
        }
      }
    }
  }

  private static PGSimpleDataSource buildDataSource(PostgreSQLContainer<?> container) {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setServerNames(new String[] {container.getHost()});
    ds.setPortNumbers(new int[] {container.getMappedPort(5432)});
    ds.setDatabaseName(container.getDatabaseName());
    ds.setUser(container.getUsername());
    ds.setPassword(container.getPassword());
    return ds;
  }
}
