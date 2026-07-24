// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.carbonio.message.dispatcher.auth.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.user_management.sdk.rest.ApiClient;
import com.zextras.carbonio.user_management.sdk.rest.api.UserResourceApi;
import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;

/**
 * Guards against a regression that crash-looped message-dispatcher-auth at startup:
 *
 * <pre>
 * java.lang.NoClassDefFoundError: com/fasterxml/jackson/databind/ser/std/ToEmptyObjectSerializer
 *   at com.zextras.carbonio.user_management.sdk.rest.ApiClient.createDefaultObjectMapper(ApiClient.java:206)
 * </pre>
 *
 * <p>{@code ToEmptyObjectSerializer} was only added in jackson-databind 2.16. The
 * carbonio-user-management-rest-sdk is generated/compiled against a much newer jackson-databind
 * (2.22.x), but without an explicit dependency management pin, Maven's "nearest wins" conflict
 * resolution let {@code com.orbitz.consul:consul-client}'s transitive {@code
 * jackson-databind:2.12.0} win over the REST SDK's own {@code jackson-databind:2.22.0}, so the
 * auth fatjar bundled a jackson-databind too old to contain the class the SDK needs.
 *
 * <p>{@link com.zextras.carbonio.message.dispatcher.auth.config.MessageDispatcherModule
 * #provideUserResourceApi} builds the real {@link ApiClient} (unlike the rest of the test suite,
 * which mocks {@link UserResourceApi} directly and therefore never touches this code path), so
 * this test is the only one that would have caught the bug: it must be run with the module's
 * actual runtime classpath, not a mock.
 */
class UserManagementApiClientConstructionTest {

  @Test
  void createDefaultObjectMapperDoesNotThrowNoClassDefFoundError() {
    ObjectMapper mapper = assertDoesNotThrow(ApiClient::createDefaultObjectMapper);
    assertNotNull(mapper);
  }

  @Test
  void apiClientConstructionDoesNotThrow() {
    HttpClient.Builder httpClientBuilder =
        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1);

    ApiClient apiClient =
        assertDoesNotThrow(
            () ->
                new ApiClient(
                    httpClientBuilder, ApiClient.createDefaultObjectMapper(), "http://localhost:20000"));

    assertNotNull(apiClient);
  }

  @Test
  void userResourceApiConstructionDoesNotThrow() {
    HttpClient.Builder httpClientBuilder =
        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1);
    ApiClient apiClient =
        new ApiClient(httpClientBuilder, ApiClient.createDefaultObjectMapper(), "http://localhost:20000");

    UserResourceApi userResourceApi = assertDoesNotThrow(() -> new UserResourceApi(apiClient));

    assertNotNull(userResourceApi);
  }
}
