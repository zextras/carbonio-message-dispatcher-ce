// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.dal.impl;

import com.google.inject.Inject;
import com.zextras.carbonio.message.dispatcher.auth.dal.DatabaseManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManagerFlyway implements DatabaseManager {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseManagerFlyway.class);

  private final DataSource dataSource;
  private Flyway flyway;

  @Inject
  public DatabaseManagerFlyway(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void initialize() {
    Optional<Integer> legacyVersion = getLegacyDatabaseVersion();

    FluentConfiguration config = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .configuration(Map.of("flyway.postgresql.transactional.lock", "false"));

    legacyVersion.ifPresent(version -> {
      logger.info("Legacy database version detected: {}. Setting as Flyway baseline.", version);
      config.baselineVersion(String.valueOf(version)).baselineOnMigrate(true);
    });

    flyway = config.load();
    flyway.migrate();
    logger.info("Database migration completed. Current version: {}", getDatabaseVersion());
  }

  /**
   * Checks for legacy database_version table and maps version to Flyway integer.
   *
   * Mapping:
   *   6.0.0 → 1 (initial schema)
   *   6.2.0 → 2 (discovery nodes)
   *   6.2.1 → 3 (roster, mam, caps)
   *   6.3.2 → 4 (fast auth token)
   *
   * @return Flyway baseline version, or empty if database is new
   */
  private Optional<Integer> getLegacyDatabaseVersion() {
    String sql = "SELECT version FROM database_version LIMIT 1";
    try (Connection conn = dataSource.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
      if (rs.next()) {
        String legacyVersion = rs.getString("version");
        int flywayVersion = mapLegacyVersionToFlyway(legacyVersion);
        logger.info("Legacy database_version found: {} → Flyway baseline: {}",
            legacyVersion, flywayVersion);
        return Optional.of(flywayVersion);
      }
      return Optional.empty();
    } catch (SQLException e) {
      logger.debug("No legacy database_version table found (expected for new installations)");
      return Optional.empty();
    }
  }

  /**
   * Maps legacy semantic versions to Flyway integer versions.
   */
  private int mapLegacyVersionToFlyway(String legacyVersion) {
    return switch (legacyVersion) {
      case "6.0.0" -> 1;
      case "6.2.0" -> 2;
      case "6.2.1" -> 3;
      case "6.3.2" -> 4;
      default -> {
        logger.warn("Unknown legacy version: {}. Defaulting to baseline 1.", legacyVersion);
        yield 1;
      }
    };
  }

  @Override
  public String getDatabaseVersion() {
    if (flyway == null) {
      return "0";
    }
    return flyway.info().current() != null
        ? flyway.info().current().getVersion().getVersion()
        : "0";
  }

  @Override
  public boolean isDatabaseLive() {
    try (Connection connection = dataSource.getConnection()) {
      return connection.isValid(1);
    } catch (SQLException e) {
      logger.error("Database liveness check failed", e);
      return false;
    }
  }

  @Override
  public boolean isDatabaseCorrectVersion() {
    return flyway != null
        && flyway.info().current() != null
        && !flyway.info().current().getPhysicalLocation().isEmpty();
  }
}