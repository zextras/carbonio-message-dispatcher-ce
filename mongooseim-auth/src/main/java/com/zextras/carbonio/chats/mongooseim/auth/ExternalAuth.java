package com.zextras.carbonio.chats.mongooseim.auth;

import com.zextras.carbonio.usermanagement.UserManagementClient;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

/**
 * Simple auth implementation. If this complicates further, we might need to make this more testable.
 */
public class ExternalAuth {

  private final InputStream          erlangStream;
  private final OutputStream         erlangOutputStream;
  private final UserManagementClient userManagementClient;

  public ExternalAuth(
    InputStream erlangStream, OutputStream erlangOutputStream, UserManagementClient userManagementClient
  ) {
    this.erlangStream = erlangStream;
    this.erlangOutputStream = erlangOutputStream;
    this.userManagementClient = userManagementClient;
  }

  public void startLoop() throws Exception {
    short length;
    while ((length = readErlangPktLength()) > 0) {
      String[] commands = readErlangCommand(length).map(command -> command.split(":")).orElse(new String[]{"unknown"});
      String user, password;
      switch (commands[0]) {
        case "auth":
          user = commands[1];
          password = commands[3];
          writeResult(
            userManagementClient.validateUserToken(password).filter(userId -> userId.getUserId().equals(user))
              .onFailure(ex -> System.err.printf("Error while authenticating a user: %s%n", ex))
              .isSuccess()
          );
          break;
        case "isuser":
          //TODO this needs some help from the user management team
          System.err.println("Is user received: " + Arrays.toString(commands));
          writeResult(true);
          break;
        default:
          System.err.printf("An unknown message was received: %s%n", commands[0]);
          writeResult(false);
          break;
      }
    }
  }

  private short readErlangPktLength() throws Exception {
    byte[] arr = new byte[2];
    int read = erlangStream.read(arr, 0, 2);
    if (read > 0) {
      return ByteBuffer.wrap(arr).getShort();
    } else {
      return 0;
    }
  }

  private Optional<String> readErlangCommand(short commandLength) throws Exception {
    byte[] arr = new byte[commandLength];
    int read = erlangStream.read(arr, 0, commandLength);
    if (read > 0) {
      return Optional.of(new String(arr));
    }
    return Optional.empty();
  }

  private void writeResult(boolean result) throws Exception {
    if (result) {
      erlangOutputStream.write(new byte[]{0, 2, 0, 1});
    } else {
      erlangOutputStream.write(new byte[]{0, 2, 0, 0});
    }
  }

}
