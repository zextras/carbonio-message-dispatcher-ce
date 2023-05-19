// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth;

import com.zextras.carbonio.message.dispatcher.auth.config.Constant;
import com.zextras.carbonio.message.dispatcher.auth.service.AuthenticationService;
import com.zextras.carbonio.message.dispatcher.auth.service.impl.AuthenticationServiceImpl;
import com.zextras.carbonio.message.dispatcher.auth.web.api.CheckPasswordApi;
import com.zextras.carbonio.message.dispatcher.auth.web.api.HealthApi;
import com.zextras.carbonio.usermanagement.UserManagementClient;

import java.net.InetSocketAddress;
import javax.servlet.ServletRegistration.Dynamic;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zextras.carbonio.message.dispatcher.auth.web.api.UserExistsApi;

public class Boot {

  private final AuthenticationService authenticationService;

  public Boot(){
    this.authenticationService = new AuthenticationServiceImpl(getUserManagementClient());
  }

  private UserManagementClient getUserManagementClient() {
    return UserManagementClient.atURL(
      String.format("http://%s:%s",
        Constant.SERVER_HOST,
        Constant.USER_MANAGEMENT_PORT));
  }

  public void boot() throws Exception {
    Server server = new Server(new InetSocketAddress(Constant.SERVER_HOST, Constant.SERVER_PORT));
    ContextHandlerCollection handlers = new ContextHandlerCollection();
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

    context.addServletContainerInitializer((c, ctx) -> {
      Dynamic checkPassword = ctx.addServlet("CheckPasswordServlet",
        CheckPasswordApi.create(authenticationService));
      checkPassword.addMapping("/check_password");
    });
    context.addServletContainerInitializer((c, ctx) -> {
      Dynamic userExists = ctx.addServlet("UserExistsServlet", UserExistsApi.create(authenticationService));
      userExists.addMapping("/user_exists");
    });
    context.addServletContainerInitializer((c, ctx) -> {
      Dynamic health = ctx.addServlet("HealthServlet", HealthApi.create());
      health.addMapping("/health/ready");
    });
    handlers.addHandler(context);
    server.setHandler(handlers);
    server.start();
    server.join();
  }
}
