// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.zextras.carbonio.message.dispatcher.auth.config.Constant;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.LoggerFactory;

public class Main {

  public static void main(String[] args) throws Exception {
    if (Files.exists(Path.of(Constant.LOGGER_CONFIG_PATH))) {
      loadLoggingConfigurations();
    }
    new Boot().boot();
  }

  private static void loadLoggingConfigurations() {
    System.out.printf("Loading logging configurations from file '%s' ...%n", Constant.LOGGER_CONFIG_PATH);
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(context);
    context.reset();
    try {
      configurator.doConfigure(Constant.LOGGER_CONFIG_PATH);
      System.out.println("Logging configurations file loaded");
    } catch (JoranException e1) {
      System.out.println("Failed to load logging configurations file");
      e1.printStackTrace();
      try {
        System.out.println("Loading logging console configurations");
        configurator.doConfigure(Main.class.getClassLoader().getResourceAsStream("logback.xml"));
        System.out.println("Logging console configurations loaded");
      } catch (JoranException e2) {
        System.out.println("Failed to load logging console configurations");
        e2.printStackTrace();
        System.out.println("WARN: No logging was started");
      }
    }
  }
}
