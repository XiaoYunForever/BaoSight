package com.baoSight.service.caseAutoRunner.protocol;


import com.baoSight.DTO.model.execution.CaseExecutionRequest;
import com.baoSight.DTO.model.execution.CaseExecutionResult;
import com.baoSight.DTO.model.execution.opcua.OpcUaRequestData;
import com.baoSight.service.caseAutoRunner.BrowserFactory;
import com.baoSight.service.caseAutoRunner.BrowserSession;
import org.springframework.beans.factory.annotation.Value;
import java.util.Locale;

/** 公共执行模板：准备会话 → 执行具体用例 → 按配置收尾。 */
public abstract class AbstractBrowserCase implements Protocol {
    @Value("${plc.browser}")
    private String browserName;

    @Value("${plc.browser.keepOpen}")
    private boolean keepOpen;

    @Value("${plc.ide.url}")
    private String ideUrl;

    @Override
    public final CaseExecutionResult start(CaseExecutionRequest caseExecutionRequest) throws Exception {
        BrowserFactory.Browser preferred = BrowserFactory.Browser.valueOf(
                browserName.toUpperCase(Locale.ROOT));

        BrowserFactory.Browser fallback;

        if (preferred == BrowserFactory.Browser.CHROME) {
            fallback = BrowserFactory.Browser.EDGE;
        } else {
            fallback = BrowserFactory.Browser.CHROME;
        }

        // 会话为局部变量，Spring 单例用例对象不保存 driver，避免并发串用。
        BrowserSession session = BrowserSession.open(ideUrl, preferred, fallback);
        // keepopen 为true 则案例结束后自动关闭浏览器

        if (keepOpen) {
            CaseExecutionResult result= executeCase(session,caseExecutionRequest);
            return result;
        } else {
            try (BrowserSession managed = session) {
                CaseExecutionResult result= executeCase(managed,caseExecutionRequest);
                return result;
            }
        }
    }

    @Override
    public OpcUaRequestData opcuaDataBuilder(CaseExecutionRequest caseExecutionRequest){
            OpcUaRequestData opcUaRequestData = new OpcUaRequestData();
            opcUaRequestData.setCaseName(caseExecutionRequest.getCaseCode());
            opcUaRequestData.setMode(caseExecutionRequest.getMode());
        return opcUaRequestData;
    }

    protected abstract CaseExecutionResult executeCase(BrowserSession session,CaseExecutionRequest caseExecutionRequest) throws Exception;
}
