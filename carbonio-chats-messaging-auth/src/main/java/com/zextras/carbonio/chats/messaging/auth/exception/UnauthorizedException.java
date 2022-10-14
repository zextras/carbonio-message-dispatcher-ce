package com.zextras.carbonio.chats.messaging.auth.exception;

public class UnauthorizedException extends RuntimeException {

  private static UnauthorizedException create() {
    return new UnauthorizedException();
  }

}
