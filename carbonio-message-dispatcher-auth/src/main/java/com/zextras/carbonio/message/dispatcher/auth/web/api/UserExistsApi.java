// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.web.api;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserExistsApi extends HttpServlet {

  public UserExistsApi() {}

  public static UserExistsApi create() {
    return new UserExistsApi();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.getWriter().print(true);
    response.setContentLength(4);
    response.setStatus(200);
  }
}
