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
            String password,
            SecurityPolicy securityPolicy,
            MessageSecurityMode securityMode) throws Exception {

        Objects.requireNonNull(endpointUrl, "服务器地址不能为空");
        Objects.requireNonNull(securityPolicy, "安全策略不能为空");
        Objects.requireNonNull(securityMode, "消息安全模式不能为空");

        boolean anonymous = username == null || username.trim().isEmpty();

        if (!anonymous) {
            Objects.requireNonNull(password, "用户名密码登录时密码不能为空");
        }

        // None 安全策略必须搭配 None 消息安全模式。
        if ((securityPolicy == SecurityPolicy.None)
                != (securityMode == MessageSecurityMode.None)) {
            throw new IllegalArgumentException(
                    "None 安全策略必须搭配 None 消息安全模式；"
                            + "其他安全策略必须搭配 Sign 或 SignAndEncrypt");
        }

        UserTokenType tokenType = anonymous
                ? UserTokenType.Anonymous
                : UserTokenType.UserName;
        IdentityProvider identityProvider = anonymous
                ? new AnonymousProvider()
                : new UsernameProvider(username, password);

        OpcUaClient client = OpcUaClient.create(
                endpointUrl.trim(),

                endpoints -> endpoints.stream()
                        // 匹配传入的安全策略。
                        .filter(endpoint ->
                                securityPolicy.getUri().equals(
                                        endpoint.getSecurityPolicyUri()))

                        // 匹配传入的消息安全模式。
                        .filter(endpoint ->
                                securityMode.equals(endpoint.getSecurityMode()))

                        // 匹配匿名或用户名密码登录方式。
                        .filter(endpoint ->
                                endpoint.getUserIdentityTokens() != null
                                        && Arrays.stream(endpoint.getUserIdentityTokens())
                                        .anyMatch(token ->
                                                tokenType.equals(token.getTokenType())))

                        .findFirst(),

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
            client.connect().get(10, TimeUnit.SECONDS);
            return client;
        } catch (Exception connectError) {
            if (connectError instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

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
