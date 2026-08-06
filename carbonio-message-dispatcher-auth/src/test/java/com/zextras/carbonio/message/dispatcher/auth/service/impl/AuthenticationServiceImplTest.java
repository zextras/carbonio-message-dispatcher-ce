// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zextras.carbonio.message.dispatcher.auth.exception.UnauthorizedException;
import com.zextras.carbonio.user_management.sdk.rest.ApiException;
import com.zextras.carbonio.user_management.sdk.rest.api.UserResourceApi;
import com.zextras.carbonio.user_management.sdk.rest.model.MyselfDto;
import com.zextras.carbonio.user_management.sdk.rest.model.UserInfoDto;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AuthenticationServiceImpl}, proving the gRPC to REST SDK swap: {@link
 * UserResourceApi#internalUsersMyselfGet(String)} is called with the auth token as an explicit
 * header param instead of the old blocking stub, and {@link ApiException} status codes are mapped
 * the same way the old {@code StatusRuntimeException} codes were.
 */
class AuthenticationServiceImplTest {

  @Test
  void validateTokenReturnsUserIdOnValidToken() throws Exception {
    UserResourceApi userResourceApi = mock(UserResourceApi.class);
    UserInfoDto userInfo = new UserInfoDto().userId("user-123");
    MyselfDto myself = new MyselfDto().info(userInfo);
    when(userResourceApi.internalUsersMyselfGet(null, "valid-token")).thenReturn(myself);

    AuthenticationServiceImpl authenticationService =
        new AuthenticationServiceImpl(userResourceApi);

    Optional<String> userId = authenticationService.validateToken("valid-token");

    assertTrue(userId.isPresent());
    assertEquals("user-123", userId.get());
  }

  @Test
  void validateTokenThrowsUnauthorizedOn401() throws Exception {
    UserResourceApi userResourceApi = mock(UserResourceApi.class);
    when(userResourceApi.internalUsersMyselfGet(null, "bad-token"))
        .thenThrow(new ApiException(401, "Unauthorized"));

    AuthenticationServiceImpl authenticationService =
        new AuthenticationServiceImpl(userResourceApi);

    assertThrows(
        UnauthorizedException.class, () -> authenticationService.validateToken("bad-token"));
  }
}
