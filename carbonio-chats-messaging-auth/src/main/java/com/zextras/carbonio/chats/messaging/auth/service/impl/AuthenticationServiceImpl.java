package service.impl;

import com.zextras.carbonio.chats.messaging.auth.exception.FailedDependencyException;
import com.zextras.carbonio.chats.messaging.auth.exception.UnauthorizedException;
import com.zextras.carbonio.usermanagement.UserManagementClient;
import com.zextras.carbonio.usermanagement.entities.UserId;
import com.zextras.carbonio.usermanagement.exceptions.Unauthorized;
import io.vavr.control.Try;
import java.util.Optional;
import service.AuthenticationService;

public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserManagementClient userManagementClient;

  public AuthenticationServiceImpl(UserManagementClient userManagementClient) {
    this.userManagementClient = userManagementClient;
  }

  @Override
  public Optional<String> validateToken(String token) {
    Try<UserId> userId = userManagementClient.validateUserToken(token);
    if (userId.isSuccess()) {
      return Optional.of(userId.get().getUserId());
    }
    if (userId.isFailure()) {
      if (userId.getCause() instanceof Unauthorized) {
        throw new UnauthorizedException();
      }else {
        throw new FailedDependencyException();
      }
    }
    return Optional.empty();
  }


}
