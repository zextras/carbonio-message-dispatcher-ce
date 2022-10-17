package com.zextras.carbonio.chats.messaging.auth.exception;

public class FailedDependencyException extends RuntimeException {

  private static FailedDependencyException create() {
    return new FailedDependencyException();
  }

}
