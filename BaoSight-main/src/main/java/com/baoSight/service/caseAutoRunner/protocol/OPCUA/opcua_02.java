package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.service.caseAutoRunner.protocol.AbstractBrowserCase;
import com.baoSight.service.caseAutoRunner.BrowserSession;
import org.springframework.stereotype.Service;

/** OPC UA 用例 02：只负责编排本用例的操作步骤。 */
@Service("case_opcua_02")
public class opcua_02 extends AbstractBrowserCase {
    @Override
    protected void executeCase(BrowserSession session) {
        opcua_atom actions = new opcua_atom(session.getDriver());
        // TODO 按用例 02 的定义组合操作；以下仅演示调用现有占位方法。
        actions.initialization();
        actions.opcuaInitial();
    }
}
