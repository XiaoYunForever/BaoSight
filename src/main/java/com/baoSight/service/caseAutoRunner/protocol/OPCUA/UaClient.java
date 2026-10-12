package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.service.caseAutoRunner.protocol.OPCUA.opcuaSever.OpcuaSimulationServer;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;

import java.util.List;

public class UaClient {
    private UaClient() {};

    static String URI;
    static String Username;
    static String Password;
    static SecurityPolicy securityPolicy;
    static MessageSecurityMode  messageSecurityMode;
    //匿名测试
    public static OpcUaClient UaConnect(String mode,String cerMode,String userName,String password) throws Exception {
        //simulation 创建虚拟opcua服务器
        URI = "opc.tcp://milo.digitalpetri.com:62541/milo";
        Username = userName;
        Password = password;

        if(mode.equals("0")){
            //URI == ??
            //Username == ?
            // Password == ?
            OpcuaSimulationServer opcuaSimulationServer =  new OpcuaSimulationServer();
            opcuaSimulationServer.setup();
        }
        //NO Cert
        if(cerMode.equals("0")){
            securityPolicy = SecurityPolicy.None;
            messageSecurityMode = MessageSecurityMode.None;
        }
        //Cert Sign&&Encrypt
        if(cerMode.equals("1")){
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
}
