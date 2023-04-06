// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.web.api;

import com.zextras.carbonio.message.dispatcher.auth.service.AuthenticationService;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserExistsApi extends HttpServlet {

  private final AuthenticationService authenticationService;

  public UserExistsApi(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  public static UserExistsApi create(AuthenticationService authenticationService) {
    return new UserExistsApi(authenticationService);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.getWriter().print(true);
    response.setContentLength(4);
    response.setStatus(200);
  }
}
