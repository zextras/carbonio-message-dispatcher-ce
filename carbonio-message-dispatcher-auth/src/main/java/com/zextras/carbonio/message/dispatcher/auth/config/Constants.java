// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.config;

public final class Constants {

  private Constants() {}

  public static final String LOGGER_CONFIG_PATH = "/etc/carbonio/message-dispatcher/logback.xml";

  public static final class Server {

    private Server() {}

    public static final String DEFAULT_HOST = "127.78.0.23";
    public static final int DEFAULT_PORT = 10000;
    public static final String HOST_PROPERTY = "carbonio.message-dispatcher.auth.host";
    public static final String PORT_PROPERTY = "carbonio.message-dispatcher.auth.port";
  }

  public static final class UserManagement {

    private UserManagement() {}

    public static final String DEFAULT_HOST = "127.78.0.23";
    public static final int DEFAULT_PORT = 20000;
    public static final String HOST_PROPERTY = "carbonio.user-management.host";
    public static final String PORT_PROPERTY = "carbonio.user-management.port";
  }

  public static final class Database {

    private Database() {}

    public static final String DEFAULT_HOST = "127.78.0.23";
    public static final int DEFAULT_PORT = 20001;
    public static final String HOST_PROPERTY = "carbonio.postgres.host";
    public static final String PORT_PROPERTY = "carbonio.postgres.port";

    public static final String CONSUL_SERVICE_NAME = "carbonio-message-dispatcher-db";
    public static final String NAME_KEY = "db-name";
    public static final String USERNAME_KEY = "db-username";
    public static final String PASSWORD_KEY = "db-password";
    public static final String DEFAULT_NAME = "carbonio-message-dispatcher-db";
    public static final String DEFAULT_USERNAME = "carbonio-message-dispatcher-db";
    public static final String DEFAULT_PASSWORD = "";
  }
}