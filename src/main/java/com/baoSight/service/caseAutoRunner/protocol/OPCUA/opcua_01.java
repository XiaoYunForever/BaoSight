package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.DTO.model.execution.CaseExecutionRequest;
import com.baoSight.DTO.model.execution.CaseExecutionResult;
import com.baoSight.DTO.model.execution.opcua.OpcUaCaseData;
import com.baoSight.enums.ExecutionMode;
import com.baoSight.service.caseAutoRunner.protocol.AbstractBrowserCase;
import com.baoSight.service.caseAutoRunner.BrowserSession;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/** OPC UA 用例 01：只负责编排本用例的操作步骤。 */
@Service("case_opcua_01")
public class opcua_01 extends AbstractBrowserCase {
//    每一次案例需要selenium资源，这部分的前置操作是重复的，所以抽象出来到AbstractBrowserCase。executeCase方法只要专注原子操作排列组合就行
//     调用抽象类的start(公共部分抽象)，然后start方法调用executeCase，子类具体实现executeCase操作
//     一次测试案例操作，需要一次session资源，传入对应的session


    @Override
    protected CaseExecutionResult executeCase(BrowserSession session, CaseExecutionRequest caseExecutionRequest) throws Exception {

        String caseName = "default";

        opcua_atom actions = new opcua_atom(session.getDriver());
        // 原子操作
        actions.initialization();
        actions.opcuaInitial();
        actions.port("48488");//48400-48499
        actions.subscription();
        //actions.securityStrategy();
        //actions.certification();
        //actions.userAdministration("admin1","admin123");
        actions.loadingPrograms(caseName);
        actions.simAndDownload();
        //ua 访问虚拟opcUA服务器
        OpcUaCaseData  opcUaCaseData = new OpcUaCaseData();

        //解析数据
        ExecutionMode exmode = caseExecutionRequest.getMode();

        // 只启动虚拟服务器，不看变量表
        UaClient.ConnectConfig connectConfig = UaClient.ConnectConfig.builder()
                .userName(null)
                .password(null)
                .cerMode("0")
                .mode(exmode)
                .actions(null)
                //.caseName(caseName)
                .build();
        OpcUaClient client = null;
        try {
            client = UaClient.UaConnect(connectConfig);
            opcUaCaseData.setConnected(true);
        } catch (InterruptedException e) { //等待连接时，线程被中断
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) { // 连接失败
            opcUaCaseData.setConnected(false);
            opcUaCaseData.setServerTimeText(null);
            System.err.println("OPC UA 连接失败，跳过读取并继续后续流程：" + e.getMessage());
        }

        if (client != null) {
        try {
            DataValue value = client.readValue(
                    0.0,
                    TimestampsToReturn.Both,
                    Identifiers.Server_ServerStatus_CurrentTime
            ).get(15, TimeUnit.SECONDS);

            System.out.println("读取状态：" + value.getStatusCode());
            System.out.println("服务器时间：" + value.getValue().getValue());
            opcUaCaseData.setServerTimeText(String.valueOf(value.getValue().getValue()));
        } finally {
            client.disconnect().get(5, TimeUnit.SECONDS);
        }
        }




        CaseExecutionResult r = new CaseExecutionResult();
        r.setOpcUaCaseData(opcUaCaseData);
        return r;
    }
}
