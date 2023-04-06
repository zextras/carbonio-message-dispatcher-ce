// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.utility;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Utilities {

  public static Map<String, String> getQueryItems(String queryString) {
    Map<String, String> map = new HashMap<>();
    if (queryString == null) {
      return map;
    }

    Arrays.stream(queryString.split("&")).forEach(item -> {
        if (item.contains("=")) {
          String[] kv = item.split("=", 2);
          if (!kv[1].equals("undefined")) {
            map.put(kv[0], kv[1]);
          }
        }
      }
    );
    return map;
  }

}
