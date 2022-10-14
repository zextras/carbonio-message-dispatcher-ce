package com.zextras.carbonio.chats.messaging.auth.service;

import java.util.Optional;

public interface AuthenticationService {

  Optional<String> validateToken(String token);
}
