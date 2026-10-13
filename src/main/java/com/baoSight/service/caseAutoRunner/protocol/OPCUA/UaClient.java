package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.DTO.model.execution.opcua.OpcUaRequestData;
import com.baoSight.enums.ExecutionMode;
import com.baoSight.service.caseAutoRunner.protocol.OPCUA.opcuaSever.OpcuaSimulationServer;
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

    public static OpcUaClient UaConnect(OpcUaRequestData opcUaRequestData) throws Exception {
        //初始化默认值
        URI = "opc.tcp://milo.digitalpetri.com:62541/milo";
        Username = opcUaRequestData.getUsername();
        Password = opcUaRequestData.getPassword();

        //simulation 创建虚拟opcua服务器
        if(opcUaRequestData.getMode().equals(ExecutionMode.SIMULATION)){
            OpcuaSimulationServer opcuaSimulationServer =  new OpcuaSimulationServer();
            opcuaSimulationServer.setup(opcUaRequestData.getActions(), opcUaRequestData.getCaseName());
            URI = opcuaSimulationServer.getEndpointUrl();
        }
        //NO Cert
        if(opcUaRequestData.getCertMode().equals("0")){
            securityPolicy = SecurityPolicy.None;
            messageSecurityMode = MessageSecurityMode.None;
        }
        //Cert Sign&&Encrypt
        if(opcUaRequestData.getCertMode().equals("1")){
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
