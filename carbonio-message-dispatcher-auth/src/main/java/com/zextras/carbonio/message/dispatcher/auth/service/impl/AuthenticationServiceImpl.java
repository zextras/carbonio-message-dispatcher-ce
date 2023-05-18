// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.service.impl;

import com.zextras.carbonio.message.dispatcher.auth.exception.FailedDependencyException;
import com.zextras.carbonio.message.dispatcher.auth.exception.UnauthorizedException;
import com.zextras.carbonio.message.dispatcher.auth.service.AuthenticationService;
import com.zextras.carbonio.usermanagement.UserManagementClient;
import com.zextras.carbonio.usermanagement.entities.UserId;
import com.zextras.carbonio.usermanagement.exceptions.Unauthorized;
import io.vavr.control.Try;
import java.util.Optional;

public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserManagementClient userManagementClient;

  public AuthenticationServiceImpl(UserManagementClient userManagementClient) {
    this.userManagementClient = userManagementClient;
  }

  @Override
  public Optional<String> validateToken(String token) {
    Try<UserId> userId = userManagementClient.validateUserToken(token);
    if (userId.isSuccess()) {
      System.out.println("Validated user with id: " + userId.get().getUserId());
      return Optional.of(userId.get().getUserId());
    }
    if (userId.isFailure()) {
      if (userId.getCause() instanceof Unauthorized) {
        throw new UnauthorizedException();
      } else {
        throw new FailedDependencyException();
      }
    }
    return Optional.empty();
  }
}
