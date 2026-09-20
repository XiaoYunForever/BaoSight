package com.baoSight.service.caseAutoRunner;

import org.openqa.selenium.WebDriver;
import java.net.URI;

/** 一次用例的浏览器会话；只负责准备环境及释放资源。 */
public final class BrowserSession implements AutoCloseable {
    private final WebDriver driver;

    private BrowserSession(WebDriver driver) {
        this.driver = driver;
    }

    public static BrowserSession open(String ideUrl, BrowserFactory.Browser... priority) {
        if (ideUrl == null || ideUrl.isBlank()) {
            throw new IllegalArgumentException("请通过 -Dplc.ide.url 配置 IDE 地址");
        }
        URI uri = URI.create(ideUrl);
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
            throw new IllegalArgumentException("IDE 地址必须是完整的 HTTP/HTTPS 地址");
        }
        WebDriver driver = BrowserFactory.createBrowser(priority);
        try {
            driver.manage().window().maximize();
            driver.get(ideUrl);
            return new BrowserSession(driver);
        } catch (RuntimeException e) {
            try { driver.quit(); } catch (RuntimeException cleanup) { e.addSuppressed(cleanup); }
            throw e;
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public void close() {
        driver.quit();
    }
}
