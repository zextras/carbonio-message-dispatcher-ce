// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zextras.carbonio.message.dispatcher.auth.dal.DatabaseManager;
import com.zextras.carbonio.message.dispatcher.auth.dal.impl.DatabaseManagerFlyway;
import com.zextras.carbonio.message.dispatcher.auth.service.AuthenticationService;
import com.zextras.carbonio.message.dispatcher.auth.service.impl.AuthenticationServiceImpl;
import com.zextras.carbonio.user_management.sdk.rest.ApiClient;
import com.zextras.carbonio.user_management.sdk.rest.api.UserResourceApi;
import java.net.http.HttpClient;
import java.time.Duration;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDispatcherModule extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(MessageDispatcherModule.class);

  /**
   * Connect/read timeout for the user-management REST client. Hardcoded rather than exposed as
   * config by explicit team decision. 5000ms matches this codebase's existing convention for a
   * shared client calling another Carbonio service over the mesh: {@code
   * HttpClientProvider.TIMEOUT_MILLIS} is exactly 5000 in both carbonio-ws-collaboration and
   * carbonio-notification-push.
   */
  private static final Duration USER_MANAGEMENT_TIMEOUT = Duration.ofSeconds(5);

  @Override
  protected void configure() {
    bind(DatabaseManager.class).to(DatabaseManagerFlyway.class).in(Singleton.class);
    bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
  }

  @Provides
  @Singleton
  public MessageDispatcherConfig provideConfig() throws Exception {
    MessageDispatcherConfig config = new MessageDispatcherConfig();
    config.loadConfig();
    return config;
  }

  @Provides
  @Singleton
  public DataSource provideDataSource(MessageDispatcherConfig config) {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setServerNames(new String[]{config.getDatabaseHost()});
    dataSource.setPortNumbers(new int[]{Integer.parseInt(config.getDatabasePort())});
    dataSource.setDatabaseName(config.getDatabaseName());
    dataSource.setUser(config.getDatabaseUsername());
    dataSource.setPassword(config.getDatabasePassword());
    logger.info("DataSource configured for database: {}", config.getDatabaseName());
    return dataSource;
  }

  @Provides
  @Singleton
  public UserResourceApi provideUserResourceApi(MessageDispatcherConfig config) {
    String host = config.getUserManagementHost();
    int port = config.getUserManagementPort();
    String baseUrl = "http://" + host + ":" + port;
    logger.info("Creating REST client for user-management at {}", baseUrl);
    HttpClient.Builder httpClientBuilder =
        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1);
    ApiClient apiClient =
        new ApiClient(httpClientBuilder, ApiClient.createDefaultObjectMapper(), baseUrl);
    apiClient.setConnectTimeout(USER_MANAGEMENT_TIMEOUT);
    apiClient.setReadTimeout(USER_MANAGEMENT_TIMEOUT);
    return new UserResourceApi(apiClient);
  }
}
