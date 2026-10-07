package com.baoSight.service.caseAutoRunner;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import java.util.HashMap;
import java.util.Map;

/** 按参数顺序尝试浏览器，成功建立一个会话后立即返回。 */
public final class BrowserFactory {
    public enum Browser { CHROME, EDGE }

    private BrowserFactory() { }

    /*
        静态工厂，用于返回对应浏览器的driver


     */
    public static WebDriver createBrowser(Browser... priority) {
        if (priority == null || priority.length == 0) {
            throw new IllegalArgumentException("至少指定一个浏览器");
        }
        for (Browser browser : priority) {
            if (browser == null) {
                throw new IllegalArgumentException("浏览器优先级不能包含 null");
            }
        }
        WebDriverException failure = new WebDriverException("所有候选浏览器均启动失败");
        for (Browser browser : priority) {
            try {
                System.out.println("尝试启动浏览器：" + browser);
                WebDriver driver;
                switch (browser) {
                    case CHROME:
                        driver = new ChromeDriver(createBrowserOptions(ChromeOptions.class));
                        break;
                    case EDGE:
                        driver = new EdgeDriver(createBrowserOptions(EdgeOptions.class));
                        break;
                    default:
                        throw new IllegalArgumentException("不支持的浏览器：" + browser);
                }
                System.out.println("浏览器启动成功：" + browser);
                return driver;
            } catch (WebDriverException e) {
                failure.addSuppressed(e);
                System.err.println(browser + " 启动失败：" + e.getMessage());
            }
        }
        throw failure;
    }

    /** 预先允许本地 IDE 使用剪贴板，避免浏览器权限气泡挡住页面。 */
    private static <T> T createBrowserOptions(Class<T> optionsType) {
        Map<String, Object> prefs = new HashMap<>();
        // 1 = allow，避免 localhost:5571 首次访问时弹出“读取剪贴板”提示。
        prefs.put("profile.default_content_setting_values.clipboard", 1);
        if (optionsType == ChromeOptions.class) {
            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);
            return optionsType.cast(options);
        }
        EdgeOptions options = new EdgeOptions();
        options.setExperimentalOption("prefs", prefs);
        return optionsType.cast(options);
    }
}
