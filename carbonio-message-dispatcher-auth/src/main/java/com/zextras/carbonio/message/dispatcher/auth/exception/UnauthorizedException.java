// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.exception;

public class UnauthorizedException extends RuntimeException {

  private static UnauthorizedException create() {
    return new UnauthorizedException();
  }

}
