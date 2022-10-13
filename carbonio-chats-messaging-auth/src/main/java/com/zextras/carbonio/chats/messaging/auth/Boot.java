package com.zextras.carbonio.chats.messaging.auth;

import com.zextras.carbonio.chats.messaging.auth.config.Constant;
import com.zextras.carbonio.usermanagement.UserManagementClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import javax.servlet.ServletRegistration.Dynamic;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AuthenticationService;
import service.impl.AuthenticationServiceImpl;
import com.zextras.carbonio.chats.messaging.auth.servlet.CheckPasswordServlet;
import com.zextras.carbonio.chats.messaging.auth.servlet.UserExistsServlet;

public class Boot {

  private static final Logger LOGGER = LoggerFactory.getLogger(Boot.class);

  private final Properties            properties;
  private final AuthenticationService authenticationService;

  public Boot() throws IOException {
    this.properties = getProperties();
    this.authenticationService = new AuthenticationServiceImpl(getUserManagementClient());
  }

  private Properties getProperties() throws IOException {
    Properties properties = new Properties();
    System.out.printf("Loading application configurations from file '%s' ...", Constant.CONFIG_PATH);
    try {
      properties.load(new FileInputStream(Constant.CONFIG_PATH));
      LOGGER.info("Application configurations loaded");
      return properties;
    } catch (Exception e) {
      LOGGER.error("Could not load properties file: " + Constant.CONFIG_PATH);
      LOGGER.warn("Try to load the default configurations...");
    }
    try {
      properties.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
      LOGGER.warn("Application default configurations loaded");
      return properties;
    } catch (IOException e) {
      LOGGER.error("Could not load configurations");
      throw e;
    }
  }

  private UserManagementClient getUserManagementClient() {
    return UserManagementClient.atURL(
      String.format("http://%s:%s",
        properties.getProperty("user.management.host"),
        properties.getProperty("user.management.port")));
  }

  public void boot() throws Exception {
    Server server = new Server(new InetSocketAddress(Constant.SERVER_HOST, Constant.SERVER_PORT));
    ContextHandlerCollection handlers = new ContextHandlerCollection();
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

    context.addServletContainerInitializer((c, ctx) -> {
      Dynamic checkPassword = ctx.addServlet("CheckPasswordServlet",
        CheckPasswordServlet.create(authenticationService));
      checkPassword.addMapping("/check_password");
    });
    context.addServletContainerInitializer((c, ctx) -> {
      Dynamic userExists = ctx.addServlet("UserExistsServlet", UserExistsServlet.create(authenticationService));
      userExists.addMapping("/user_exists");
    });
    handlers.addHandler(context);
    server.setHandler(handlers);
    server.start();
    server.join();
  }
}
