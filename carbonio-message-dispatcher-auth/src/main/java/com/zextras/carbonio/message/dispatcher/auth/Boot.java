// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zextras.carbonio.message.dispatcher.auth.config.MessageDispatcherConfig;
import com.zextras.carbonio.message.dispatcher.auth.config.MessageDispatcherModule;
import com.zextras.carbonio.message.dispatcher.auth.dal.DatabaseManager;
import com.zextras.carbonio.message.dispatcher.auth.service.AuthenticationService;
import com.zextras.carbonio.message.dispatcher.auth.web.api.CheckPasswordApi;
import com.zextras.carbonio.message.dispatcher.auth.web.api.HealthApi;
import com.zextras.carbonio.message.dispatcher.auth.web.api.UserExistsApi;
import jakarta.servlet.ServletRegistration.Dynamic;
import java.net.InetSocketAddress;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Boot {

  private static final Logger logger = LoggerFactory.getLogger(Boot.class);

  private final Injector injector;
  private Server server;

  public Boot() {
    this.injector = Guice.createInjector(new MessageDispatcherModule());
  }

  public void boot() throws Exception {
    try {
      // Initialize database (run Flyway migrations)
      DatabaseManager databaseManager = injector.getInstance(DatabaseManager.class);
      databaseManager.initialize();

      // Get config and services
      MessageDispatcherConfig config = injector.getInstance(MessageDispatcherConfig.class);
      AuthenticationService authService = injector.getInstance(AuthenticationService.class);

      // Start the server
      server = new Server(new InetSocketAddress(config.getServerHost(), config.getServerPort()));
      ContextHandlerCollection handlers = new ContextHandlerCollection();
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

      context.addServletContainerInitializer((c, ctx) -> {
        Dynamic checkPassword = ctx.addServlet(
            "CheckPasswordServlet", CheckPasswordApi.create(authService));
        checkPassword.addMapping("/check_password");
      });

      context.addServletContainerInitializer((c, ctx) -> {
        Dynamic userExists = ctx.addServlet("UserExistsServlet", UserExistsApi.create());
        userExists.addMapping("/user_exists");
      });

      context.addServletContainerInitializer((c, ctx) -> {
        Dynamic health = ctx.addServlet("HealthServlet", HealthApi.create());
        health.addMapping("/health/ready");
      });

      handlers.addHandler(context);
      server.setHandler(handlers);
      server.start();
      logger.info("Server started on {}:{}", config.getServerHost(), config.getServerPort());
      server.join();
    } finally {
      if (server != null) {
        server.stop();
      }
    }
  }
}