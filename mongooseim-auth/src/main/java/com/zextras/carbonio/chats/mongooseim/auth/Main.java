package com.zextras.carbonio.chats.mongooseim.auth;

import com.zextras.carbonio.usermanagement.UserManagementClient;

public class Main {
  public static void main(String[] args) throws Exception {
    new ExternalAuth(System.in, System.out, UserManagementClient.atURL("http://127.78.0.10:20001")).startLoop();
  }
}
