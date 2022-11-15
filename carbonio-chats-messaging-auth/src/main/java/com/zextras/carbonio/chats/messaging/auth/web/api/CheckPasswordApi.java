package com.zextras.carbonio.chats.messaging.auth.web.api;

import com.zextras.carbonio.chats.messaging.auth.exception.FailedDependencyException;
import com.zextras.carbonio.chats.messaging.auth.exception.UnauthorizedException;
import com.zextras.carbonio.chats.messaging.auth.service.AuthenticationService;
import com.zextras.carbonio.chats.messaging.auth.utility.Utilities;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckPasswordApi extends HttpServlet {

  private final AuthenticationService authenticationService;

  public CheckPasswordApi(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  public static CheckPasswordApi create(AuthenticationService authenticationService) {
    return new CheckPasswordApi(authenticationService);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Map<String, String> queryItems = Utilities.getQueryItems(request.getQueryString());
    String user = queryItems.get("user");
    String token = queryItems.get("pass");
    if (user == null || token == null) {
      response.setStatus(400);
      response.setContentLength(0);
      return;
    }
    try {

      Optional<String> userId = authenticationService.validateToken(token);
      if (userId.isPresent()) {
        if (userId.get().equals(user)) {
          response.getWriter().print(true);
          response.setContentLength(4);
          response.setStatus(200);
        } else {
          response.getWriter().print(false);
          response.setContentLength(5);
          response.setStatus(401);
        }
      } else {
        response.getWriter().print(false);
        response.setContentLength(5);
        response.setStatus(401);
      }
    } catch (UnauthorizedException unauthorizedException) {
      response.setStatus(401);
      response.setContentLength(0);
    } catch (FailedDependencyException failedDependencyException) {
      response.setStatus(424);
      response.setContentLength(0);
    } catch (Exception e) {
      response.setStatus(500);
      response.setContentLength(0);
    }
  }
}
