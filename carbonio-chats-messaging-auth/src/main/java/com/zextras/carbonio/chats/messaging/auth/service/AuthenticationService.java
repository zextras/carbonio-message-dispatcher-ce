// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.chats.messaging.auth.service;

import java.util.Optional;

public interface AuthenticationService {

  Optional<String> validateToken(String token);
}
