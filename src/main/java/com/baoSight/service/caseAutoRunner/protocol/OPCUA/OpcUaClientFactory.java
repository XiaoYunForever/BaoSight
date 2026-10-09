package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class OpcUaClientFactory {
    private OpcUaClientFactory() {
    }

    /**
     * 创建、配置并连接 OPC UA 客户端。
     *
     * @param endpointUrl 服务器地址，例如 opc.tcp://192.168.1.100:4840
     * @param username    null 或空白表示匿名登录，否则使用用户名密码登录
     * @param password    用户名密码登录时必填
     * @return 已建立会话的客户端，使用结束后调用 disconnect()
     */
    public static OpcUaClient connect(
            String endpointUrl,
            String username,
            String password) throws Exception {

        if (endpointUrl == null
                || !endpointUrl.trim().startsWith("opc.tcp://")) {
            throw new IllegalArgumentException(
                    "endpointUrl 必须是 opc.tcp:// 开头的服务器地址");
        }

        boolean anonymous = username == null || username.trim().isEmpty();

        if (!anonymous) {
            Objects.requireNonNull(password, "用户名密码登录时 password 不能为空");
        }

        UserTokenType tokenType = anonymous
                ? UserTokenType.Anonymous
                : UserTokenType.UserName;

        IdentityProvider identityProvider = anonymous
                ? new AnonymousProvider()
                : new UsernameProvider(username, password);

        OpcUaClient client = OpcUaClient.create(
                endpointUrl.trim(),

                // 选择符合安全策略和身份认证要求的服务器端点。
                endpoints -> endpoints.stream()
                        .filter(endpoint ->
                                SecurityPolicy.None.getUri().equals(
                                        endpoint.getSecurityPolicyUri()))
                        .filter(endpoint ->
                                MessageSecurityMode.None.equals(
                                        endpoint.getSecurityMode()))
                        .filter(endpoint ->
                                endpoint.getUserIdentityTokens() != null
                                        && Arrays.stream(endpoint.getUserIdentityTokens())
                                        .anyMatch(token ->
                                                tokenType.equals(token.getTokenType())))
                        .findFirst(),

                // 端点由 create() 自动填入配置。
                config -> config
                        .setApplicationName(
                                LocalizedText.english("Java OPC UA Simulator"))
                        .setApplicationUri("urn:baosight:opcua:simulator")
                        .setIdentityProvider(identityProvider)
                        .setRequestTimeout(uint(5_000))
                        .setSessionTimeout(uint(60_000))
                        .build()
        );

        try {
            // 等待会话建立成功，成功后才返回客户端。
            client.connect().get(10, TimeUnit.SECONDS);
            return client;
        } catch (Exception connectError) {
            if (connectError instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            // 连接失败时尽力释放已创建的连接资源。
            try {
                client.disconnect().get(3, TimeUnit.SECONDS);
            } catch (Exception cleanupError) {
                if (cleanupError instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                connectError.addSuppressed(cleanupError);
            }

            throw connectError;
        }
    }
}
