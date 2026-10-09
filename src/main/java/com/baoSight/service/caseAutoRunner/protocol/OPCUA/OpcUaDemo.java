package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import java.util.concurrent.TimeUnit;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;

public class OpcUaDemo {

    public static void main(String[] args) throws Exception {
        // 匿名登录；用户名密码登录时，将 null 改为实际账号和密码。
        OpcUaClient client = OpcUaClientFactory.connect(
                "opc.tcp://milo.digitalpetri.com:62541/milo",
                "UserA",
                "password"
        );

        try {
            DataValue value = client.readValue(
                    0.0,
                    TimestampsToReturn.Both,
                    Identifiers.Server_ServerStatus_CurrentTime
            ).get(5, TimeUnit.SECONDS);

            System.out.println("读取状态：" + value.getStatusCode());
            System.out.println("服务器时间：" + value.getValue().getValue());
        } finally {
            client.disconnect().get(5, TimeUnit.SECONDS);
        }
    }
}