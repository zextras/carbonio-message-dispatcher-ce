package com.zextras.carbonio.chats.mongooseim.auth;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.zextras.carbonio.usermanagement.UserManagementClient;
import com.zextras.carbonio.usermanagement.entities.UserId;
import com.zextras.carbonio.usermanagement.exceptions.InternalServerError;
import com.zextras.carbonio.usermanagement.exceptions.Unauthorized;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ExternalAuthTest {

  private UserManagementClient  userManagementClient;
  private ErlangStream          fakeErlangStream;
  private ExternalAuth          externalAuth;
  private ByteArrayOutputStream erlangOutputStream;

  public ExternalAuthTest() {
    userManagementClient = mock(UserManagementClient.class);
    fakeErlangStream = new ErlangStream();
    erlangOutputStream = new ByteArrayOutputStream();
    externalAuth = new ExternalAuth(fakeErlangStream, erlangOutputStream, userManagementClient);
  }

  @AfterEach
  public void cleanup() {
    reset(userManagementClient);
    erlangOutputStream.reset();
  }

  @DisplayName("Authentication tests")
  @Nested
  class AuthenticationTests {

    @DisplayName("Correctly authenticates a user with user management")
    @Test
    public void authTestOk() throws Exception {
      when(userManagementClient.validateUserToken("password")).thenReturn(Try.success(new UserId("user")));
      fakeErlangStream.load("auth:user:domain:password");

      externalAuth.startLoop();

      byte[] outputBytes = erlangOutputStream.toByteArray();
      assertArrayEquals(new byte[] {0, 2, 0, 1}, outputBytes);
    }

    @DisplayName("Returns an error packet if the token is not valid")
    @Test
    public void authTestInvalidToken() throws Exception {
      when(userManagementClient.validateUserToken("password"))
        .thenReturn(Try.failure(new Unauthorized()));
      fakeErlangStream.load("auth:user:domain:password");

      externalAuth.startLoop();

      byte[] outputBytes = erlangOutputStream.toByteArray();
      assertArrayEquals(new byte[] {0, 2, 0, 0}, outputBytes);
    }

    @DisplayName("Returns an error packet if the request failed")
    @Test
    public void authTestInternalServerError() throws Exception {
      when(userManagementClient.validateUserToken("password"))
        .thenReturn(Try.failure(new InternalServerError(new RuntimeException())));
      fakeErlangStream.load("auth:user:domain:password");

      externalAuth.startLoop();

      byte[] outputBytes = erlangOutputStream.toByteArray();
      assertArrayEquals(new byte[] {0, 2, 0, 0}, outputBytes);
    }

  }

  @DisplayName("Unknown commands tests")
  @Nested
  class UnknownCommandTests {

    @DisplayName("Returns an error packet if the command is not handled")
    @Test
    public void authTestOk() throws Exception {
      when(userManagementClient.validateUserToken("password")).thenReturn(Try.success(new UserId("user")));
      fakeErlangStream.load("nocommand");

      externalAuth.startLoop();

      byte[] outputBytes = erlangOutputStream.toByteArray();
      assertArrayEquals(new byte[] {0, 2, 0, 0}, outputBytes);
    }

  }

  private static class ErlangStream extends InputStream {

    private InputStream fakeStream = null;

    public void load(String loadWith) {
      int stringLen = loadWith.length();
      if (stringLen >= Short.MAX_VALUE) {
        throw new RuntimeException("String must be less than " + Short.MAX_VALUE + "characters");
      }
      byte[] load = new byte[stringLen + 2];
      load[0] = (byte) ((stringLen & 0xff00) >> 8);
      load[1] = (byte) (stringLen & 0xff);
      System.arraycopy(loadWith.getBytes(), 0, load, 2, stringLen);
      fakeStream = new ByteArrayInputStream(load);
    }

    @Override
    public int read() throws IOException {
      if (fakeStream == null) {
        throw new RuntimeException("Not initialized");
      }
      return fakeStream.read();
    }

    @Override
    public synchronized void reset() throws IOException {
      if (fakeStream == null) {
        throw new RuntimeException("Not initialized");
      }
      fakeStream.reset();
    }
  }

}