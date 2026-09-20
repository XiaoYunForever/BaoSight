package com.baoSight.service.caseAutoRunner.protocol;

import com.baoSight.service.caseAutoRunner.BrowserFactory;
import com.baoSight.service.caseAutoRunner.BrowserSession;
import org.springframework.beans.factory.annotation.Value;
import java.util.Locale;

/** 公共执行模板：准备会话 → 执行具体用例 → 按配置收尾。 */
public abstract class AbstractBrowserCase implements Protocol {
    @Value("${plc.browser:CHROME}")
    private String browserName;

    @Value("${plc.browser.keepOpen:true}")
    private boolean keepOpen;

    @Value("${plc.ide.url:}")
    private String ideUrl;

    @Override
    public final void start() {
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
            executeCase(session);
        } else {
            try (BrowserSession managed = session) {
                executeCase(managed);
            }
        }
    }

    protected abstract void executeCase(BrowserSession session);
}
