package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.service.caseAutoRunner.atomManipulation;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

public class opcua_atom extends atomManipulation {
    public void opcuaInitial() throws InterruptedException {
        System.out.println("OPC-UA初始化开始");
        //OPCUA原子操作

        By SCROLLER = By.cssSelector(".property-tree-container [data-test-id='virtuoso-scroller']");

        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement container = driver.findElement(SCROLLER);

        long prevTop = -1;
        WebElement target = null;

        for (int i = 0; i < 400; i++) {
            // 一次 JS 调用同时：查目标 + 读 scrollTop + 滚一步
            Object r = js.executeScript(
                    "const c = arguments[0], sel = arguments[1], step = arguments[2];" +
                            "const el = document.querySelector(sel);" +
                            "if (el) return el;" +
                            "const prev = c.scrollTop;" +
                            "c.scrollTop += step;" +
                            "if (c.scrollTop === prev) return 'BOTTOM';" +
                            "return null;",
                    container, ".property-TreeNode[title='OPC UA服务器']", 200
            );

            if (r instanceof WebElement) { target = (WebElement) r; break; }
            if ("BOTTOM".equals(r)) break;

            // 只在这里等，给 Virtuoso 渲染时间——但可以短一点
            Thread.sleep(500);
        }

        if (target == null) {
            throw new NoSuchElementException("滚完没找到: OPC UA服务器");
        }

        js.executeScript("arguments[0].scrollIntoView({block:'center'});", target);
        Thread.sleep(500);
        target.click();
        System.out.println("已点击 OPC UA服务器");

        System.out.println("OPC-UA初始化完成");
    }

    public void port(){
        System.out.println("端口初始化完成");
    }

    public void subscription(){
        System.out.println("订阅初始化完成");
    }

    public void securityStrategy(){
        System.out.println("安全策略配置完成");
    }

    public void certification(){
        System.out.println("证书配置完成");
    }

    public void userAdministration(){
        System.out.println("用户管理完成");
    }
}
