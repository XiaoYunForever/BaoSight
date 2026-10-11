//package com.baoSight.service.caseAutoRunner.protocol.OPCUA;
//
//import com.baoSight.DTO.model.execution.CaseExecutionRequest;
//import com.baoSight.DTO.model.execution.CaseExecutionResult;
//import com.baoSight.service.caseAutoRunner.BrowserSession;
//import com.baoSight.service.caseAutoRunner.protocol.AbstractBrowserCase;
//import com.baoSight.service.caseAutoRunner.protocol.Protocol;
//import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
//import org.eclipse.milo.opcua.stack.core.Identifiers;
//import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
//import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.TimeUnit;
//
//@Service("case_opcua_02")
//public class opcua_02 extends AbstractBrowserCase {
//
//    @Override
//    protected CaseExecutionResult executeCase(BrowserSession session, CaseExecutionRequest caseExecutionRequest) throws Exception {
//        String caseName = "opcua02";
//
//        opcua_atom actions = new opcua_atom(session.getDriver());
//        // 原子操作
//        actions.initialization();
//        actions.opcuaInitial();
//        actions.port("4840");
//        actions.subscription();
//        actions.securityStrategy();
//        actions.certification();
//        actions.userAdministration("admin", "admin123");
//
//        //ua 访问虚拟opcUA服务器
//        OpcUaClient client =UaClient.UaConnect("1","0",null,null);
//        try {
//            DataValue value = client.readValue(
//                    0.0,
//                    TimestampsToReturn.Both,
//                    Identifiers.Server_ServerStatus_CurrentTime
//            ).get(15, TimeUnit.SECONDS);
//
//            System.out.println("读取状态：" + value.getStatusCode());
//            System.out.println("服务器时间：" + value.getValue().getValue());
//        } finally {
//            client.disconnect().get(5, TimeUnit.SECONDS);
//        }
//
//    }
//
//}
