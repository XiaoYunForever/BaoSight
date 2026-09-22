package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.service.caseAutoRunner.protocol.AbstractBrowserCase;
import com.baoSight.service.caseAutoRunner.BrowserSession;
import org.springframework.stereotype.Service;

/** OPC UA 用例 01：只负责编排本用例的操作步骤。 */
@Service("case_opcua_01")
public class opcua_01 extends AbstractBrowserCase {
//    每一次案例需要selenium资源，这部分的前置操作是重复的，所以抽象出来到AbstractBrowserCase。executeCase方法只要专注原子操作排列组合就行
//     调用抽象类的start(公共部分抽象)，然后start方法调用executeCase，子类具体实现executeCase操作
//     一次测试案例操作，需要一次session资源，传入对应的session
    @Override
    protected void executeCase(BrowserSession session) {
        opcua_atom actions = new opcua_atom(session.getDriver());
        // 原子操作
        actions.initialization();
        // 加载opcua01 下的PLC代码
        actions.loadingPrograms("opcua01");
        actions.simAndDownload();
        actions.readCPUAndLoad();
    }
}
