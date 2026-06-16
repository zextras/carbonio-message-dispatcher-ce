// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

/** Shared helpers for PostgreSQL-backed integration tests. */
public final class PostgresTestSupport {

  public static final String IMAGE = "postgres:15-alpine";

  private PostgresTestSupport() {}

  public static PostgreSQLContainer<?> newContainer() {
    return new PostgreSQLContainer<>(IMAGE);
  }

  public static DataSource dataSourceFor(PostgreSQLContainer<?> container) {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setUrl(container.getJdbcUrl());
    dataSource.setUser(container.getUsername());
    dataSource.setPassword(container.getPassword());
    return dataSource;
  }

  public static boolean tableExists(DataSource dataSource, String table) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        ResultSet tables =
            connection.getMetaData().getTables(null, null, table, new String[] {"TABLE"})) {
      return tables.next();
    }
  }
}
