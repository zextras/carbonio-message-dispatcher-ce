// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.web.api;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HealthApi extends HttpServlet {

  public HealthApi() {}

  public static HealthApi create() {
    return new HealthApi();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    response.setStatus(200);
  }
}
