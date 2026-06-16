// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.zextras.carbonio.message.dispatcher.auth.dal.impl.DatabaseManagerFlyway;
import com.zextras.carbonio.message.dispatcher.auth.exception.FailedDependencyException;
import com.zextras.carbonio.message.dispatcher.auth.exception.UnauthorizedException;
import com.zextras.carbonio.message.dispatcher.auth.service.AuthenticationService;
import com.zextras.carbonio.message.dispatcher.auth.web.api.CheckPasswordApi;
import com.zextras.carbonio.message.dispatcher.auth.web.api.HealthApi;
import com.zextras.carbonio.message.dispatcher.auth.web.api.UserExistsApi;
import jakarta.servlet.Servlet;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ServerBootIT {

  @Container static PostgreSQLContainer<?> postgres = PostgresTestSupport.newContainer();

  private static String baseUrl;
  private static HttpClient httpClient;
  private static Server server;
  private static AuthenticationService mockAuthService;

  @BeforeAll
  static void setUp() throws Exception {
    new DatabaseManagerFlyway(PostgresTestSupport.dataSourceFor(postgres)).initialize();

    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }
    baseUrl = "http://localhost:" + port;

    mockAuthService = Mockito.mock(AuthenticationService.class);

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    registerServlet(
        context,
        "CheckPasswordServlet",
        CheckPasswordApi.create(mockAuthService),
        "/check_password");
    registerServlet(context, "UserExistsServlet", UserExistsApi.create(), "/user_exists");
    registerServlet(context, "HealthServlet", HealthApi.create(), "/health/ready");

    ContextHandlerCollection handlers = new ContextHandlerCollection();
    handlers.addHandler(context);

    server = new Server(new InetSocketAddress("localhost", port));
    server.setHandler(handlers);
    server.start();

    httpClient = HttpClient.newHttpClient();
  }

  @AfterAll
  static void tearDown() throws Exception {
    if (server != null) {
      server.stop();
    }
  }

  @BeforeEach
  void resetMocks() {
    Mockito.reset(mockAuthService);
  }

  @Test
  void healthEndpointReturns200() throws Exception {
    HttpResponse<String> response = get("/health/ready");
    assertEquals(200, response.statusCode());
  }

  @Test
  void userExistsEndpointReturnsTrue() throws Exception {
    HttpResponse<String> response = get("/user_exists");
    assertEquals(200, response.statusCode());
    assertEquals("true", response.body());
  }

  @Test
  void checkPasswordReturns200WhenTokenMatchesUser() throws Exception {
    when(mockAuthService.validateToken("tok")).thenReturn(Optional.of("alice"));

    HttpResponse<String> response = get("/check_password?user=alice&pass=tok");
    assertEquals(200, response.statusCode());
    assertEquals("true", response.body());
  }

  @Test
  void checkPasswordReturns401WhenTokenBelongsToOtherUser() throws Exception {
    when(mockAuthService.validateToken("tok")).thenReturn(Optional.of("bob"));

    HttpResponse<String> response = get("/check_password?user=alice&pass=tok");
    assertEquals(401, response.statusCode());
    assertEquals("false", response.body());
  }

  @Test
  void checkPasswordReturns400WhenParamsMissing() throws Exception {
    HttpResponse<String> response = get("/check_password");
    assertEquals(400, response.statusCode());
  }

  @Test
  void checkPasswordReturns401OnUnauthorizedException() throws Exception {
    when(mockAuthService.validateToken(anyString())).thenThrow(new UnauthorizedException());

    HttpResponse<String> response = get("/check_password?user=alice&pass=tok");
    assertEquals(401, response.statusCode());
  }

  @Test
  void checkPasswordReturns424OnFailedDependency() throws Exception {
    when(mockAuthService.validateToken(anyString())).thenThrow(new FailedDependencyException());

    HttpResponse<String> response = get("/check_password?user=alice&pass=tok");
    assertEquals(424, response.statusCode());
  }

  private HttpResponse<String> get(String path) throws Exception {
    return httpClient.send(
        HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build(), BodyHandlers.ofString());
  }

  private static void registerServlet(
      ServletContextHandler context, String name, Servlet servlet, String path) {
    context.addServletContainerInitializer(
        (classes, ctx) -> ctx.addServlet(name, servlet).addMapping(path));
  }
}
