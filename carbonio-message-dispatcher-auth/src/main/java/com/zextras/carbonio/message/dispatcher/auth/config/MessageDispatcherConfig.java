// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.config;

import com.orbitz.consul.Consul;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDispatcherConfig {

  private static final Logger logger = LoggerFactory.getLogger(MessageDispatcherConfig.class);
  private static final String CONFIG_FILE_PATH = "/etc/carbonio/message-dispatcher/config.properties";

  private final Properties properties;

  public MessageDispatcherConfig() {
    properties = new Properties();
  }

  public void loadConfig() throws IOException {
    loadFromEtc().ifPresent(config -> {
      try {
        properties.load(config);
      } catch (IOException e) {
        logger.warn("Error loading configuration file: {}", e.getMessage());
      }
    });
    properties.putAll(System.getProperties());
  }

  private Optional<InputStream> loadFromEtc() {
    try {
      return Optional.of(new FileInputStream(CONFIG_FILE_PATH));
    } catch (FileNotFoundException e) {
      logger.debug("Configuration file not found at {}", CONFIG_FILE_PATH);
      return Optional.empty();
    }
  }

  public String getServerHost() {
    return properties.getProperty(
        Constants.Server.HOST_PROPERTY,
        Constants.Server.DEFAULT_HOST);
  }

  public int getServerPort() {
    return Integer.parseInt(properties.getProperty(
        Constants.Server.PORT_PROPERTY,
        String.valueOf(Constants.Server.DEFAULT_PORT)));
  }

  public String getUserManagementHost() {
    return properties.getProperty(
        Constants.UserManagement.HOST_PROPERTY,
        Constants.UserManagement.DEFAULT_HOST);
  }

  public int getUserManagementPort() {
    return Integer.parseInt(properties.getProperty(
        Constants.UserManagement.PORT_PROPERTY,
        String.valueOf(Constants.UserManagement.DEFAULT_PORT)));
  }

  public String getDatabaseHost() {
    return properties.getProperty(
        Constants.Database.HOST_PROPERTY,
        Constants.Database.DEFAULT_HOST);
  }

  public String getDatabasePort() {
    return properties.getProperty(
        Constants.Database.PORT_PROPERTY,
        String.valueOf(Constants.Database.DEFAULT_PORT));
  }

  public String getDatabaseName() {
    return getConsulConfig(Constants.Database.NAME_KEY)
        .orElse(Constants.Database.DEFAULT_NAME);
  }

  public String getDatabaseUsername() {
    return getConsulConfig(Constants.Database.USERNAME_KEY)
        .orElse(Constants.Database.DEFAULT_USERNAME);
  }

  public String getDatabasePassword() {
    return getConsulConfig(Constants.Database.PASSWORD_KEY)
        .orElse(Constants.Database.DEFAULT_PASSWORD);
  }

  private Optional<String> getConsulConfig(String key) {
    try {
      return Consul.builder()
          .withTokenAuth(System.getenv("CONSUL_HTTP_TOKEN"))
          .build()
          .keyValueClient()
          .getValueAsString(
              MessageFormat.format("{0}/{1}", Constants.Database.CONSUL_SERVICE_NAME, key));
    } catch (Exception e) {
      logger.debug("Could not fetch {} from Consul: {}", key, e.getMessage());
      return Optional.empty();
    }
  }
}