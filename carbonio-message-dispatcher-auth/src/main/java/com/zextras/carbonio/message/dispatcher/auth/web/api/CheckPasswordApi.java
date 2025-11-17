// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.web.api;

import com.zextras.carbonio.message.dispatcher.auth.exception.FailedDependencyException;
import com.zextras.carbonio.message.dispatcher.auth.exception.UnauthorizedException;
import com.zextras.carbonio.message.dispatcher.auth.service.AuthenticationService;
import com.zextras.carbonio.message.dispatcher.auth.utility.Utilities;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CheckPasswordApi extends HttpServlet {

  private final AuthenticationService authenticationService;

  public CheckPasswordApi(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  public static CheckPasswordApi create(AuthenticationService authenticationService) {
    return new CheckPasswordApi(authenticationService);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
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
