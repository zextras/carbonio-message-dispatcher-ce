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
import com.zextras.carbonio.user_management.sdk.grpc.UserManagementServiceGrpc;
import com.zextras.carbonio.user_management.sdk.grpc.UserManagementServiceGrpc.UserManagementServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDispatcherModule extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(MessageDispatcherModule.class);

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
  public ManagedChannel provideUserManagementChannel(MessageDispatcherConfig config) {
    String host = config.getUserManagementHost();
    int port = config.getUserManagementPort();
    logger.info("Creating gRPC channel to user-management at {}:{}", host, port);
    return ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();
  }

  @Provides
  @Singleton
  public UserManagementServiceBlockingStub provideUserManagementStub(
      ManagedChannel userManagementChannel) {
    return UserManagementServiceGrpc.newBlockingStub(userManagementChannel);
  }
}
