// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.service.impl;

import com.google.inject.Inject;
import com.zextras.carbonio.message.dispatcher.auth.exception.FailedDependencyException;
import com.zextras.carbonio.message.dispatcher.auth.exception.UnauthorizedException;
import com.zextras.carbonio.message.dispatcher.auth.service.AuthenticationService;
import com.zextras.carbonio.user_management.sdk.grpc.GetUserMyselfRequest;
import com.zextras.carbonio.user_management.sdk.grpc.UserManagementServiceGrpc.UserManagementServiceBlockingStub;
import com.zextras.carbonio.user_management.sdk.grpc.UserMyselfResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationServiceImpl implements AuthenticationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

  private final UserManagementServiceBlockingStub userManagementStub;

  @Inject
  public AuthenticationServiceImpl(UserManagementServiceBlockingStub userManagementStub) {
    this.userManagementStub = userManagementStub;
  }

  @Override
  public Optional<String> validateToken(String token) {
    try {
      GetUserMyselfRequest request =
          GetUserMyselfRequest.newBuilder().setToken(token).build();
      UserMyselfResponse response = userManagementStub.getUserMyself(request);
      String userId = response.getUser().getInfo().getUserId();
      LOGGER.debug("Validated user with id: {}", userId);
      return Optional.of(userId);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.Code.UNAUTHENTICATED) {
        LOGGER.debug("Failed validation for unauthorized token");
        throw new UnauthorizedException();
      }
      throw new FailedDependencyException();
    }
  }
}
