// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.service.impl;

import com.google.inject.Inject;
import com.zextras.carbonio.message.dispatcher.auth.exception.FailedDependencyException;
import com.zextras.carbonio.message.dispatcher.auth.exception.UnauthorizedException;
import com.zextras.carbonio.message.dispatcher.auth.service.AuthenticationService;
import com.zextras.carbonio.user_management.sdk.rest.ApiException;
import com.zextras.carbonio.user_management.sdk.rest.api.UserResourceApi;
import com.zextras.carbonio.user_management.sdk.rest.model.MyselfDto;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationServiceImpl implements AuthenticationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
  private static final int HTTP_UNAUTHORIZED = 401;

  private final UserResourceApi userResourceApi;

  @Inject
  public AuthenticationServiceImpl(UserResourceApi userResourceApi) {
    this.userResourceApi = userResourceApi;
  }

  @Override
  public Optional<String> validateToken(String token) {
    try {
      MyselfDto response = userResourceApi.internalUsersMyselfGet(token);
      String userId = response.getInfo().getUserId();
      LOGGER.debug("Validated user with id: {}", userId);
      return Optional.of(userId);
    } catch (ApiException e) {
      if (e.getCode() == HTTP_UNAUTHORIZED) {
        LOGGER.debug("Failed validation for unauthorized token");
        throw new UnauthorizedException();
      }
      throw new FailedDependencyException();
    }
  }
}
