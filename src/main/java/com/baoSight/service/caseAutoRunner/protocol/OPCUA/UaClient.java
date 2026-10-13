package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.enums.ExecutionMode;
import com.baoSight.service.caseAutoRunner.protocol.OPCUA.opcuaSever.OpcuaSimulationServer;
import com.baoSight.service.caseAutoRunner.protocol.atomManipulation;
import lombok.Builder;
import lombok.Data;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;

public class UaClient {
    private UaClient() {};

    static String URI;
    static String Username;
    static String Password;
    static SecurityPolicy securityPolicy;
    static MessageSecurityMode  messageSecurityMode;
    //匿名测试
    public static OpcUaClient UaConnect(ConnectConfig config) throws Exception {
        //simulation 创建虚拟opcua服务器
        URI = "opc.tcp://milo.digitalpetri.com:62541/milo";
        Username = config.userName;
        Password = config.password;

        // 仿真服务器启动
        if(config.mode == ExecutionMode.SIMULATION){
            //URI == ??
            //Username == ?
            // Password == ?
            // 默认匿名
            OpcuaSimulationServer opcuaSimulationServer =  new OpcuaSimulationServer();
            if(config.actions != null) {
                opcuaSimulationServer.setup(config.actions, config.caseName);
            }else {
                opcuaSimulationServer.setup();
            }
            URI = opcuaSimulationServer.getEndpointUrl();
        }
        //NO Cert
        if(config.cerMode.equals("0")){
            securityPolicy = SecurityPolicy.None;
            messageSecurityMode = MessageSecurityMode.None;
        }
        //Cert Sign&&Encrypt
        if(config.cerMode.equals("1")){
            securityPolicy = SecurityPolicy.Basic256Sha256;
            messageSecurityMode = MessageSecurityMode.SignAndEncrypt;
        }

        OpcUaClient client = OpcUaClientFactory.connect(
                URI,
                Username,
                Password,
                securityPolicy,
                messageSecurityMode
        );

        return client;
    }

    @Data
    @Builder
    public static class ConnectConfig {
        private ExecutionMode mode;
        @Builder.Default
        private String cerMode = "0";
        private String userName;
        private String password;

        private atomManipulation actions;
        private String caseName;

    }

}