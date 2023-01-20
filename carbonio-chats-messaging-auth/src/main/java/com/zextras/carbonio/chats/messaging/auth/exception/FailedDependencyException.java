// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.chats.messaging.auth.exception;

public class FailedDependencyException extends RuntimeException {

  private static FailedDependencyException create() {
    return new FailedDependencyException();
  }

}
