package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.service.caseAutoRunner.protocol.atomManipulation;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class opcua_atom extends atomManipulation {


    public opcua_atom(WebDriver driver) {
        super(driver); // 继承父类的driver
    }

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
        By CHECKBOX = By.cssSelector(".property-checkbox input[type='checkbox']");
        WebElement opcuabt = driver.findElement(CHECKBOX);
        opcuabt.click();

        System.out.println("OPC-UA初始化完成");
    }

    public void port(){
        //port:48400-48499
        By INPUT = By.cssSelector("input.baosky-common-number-input.property-input");
        WebElement opcport = driver.findElement(INPUT);

        opcport.clear();                          // 清空
        opcport.sendKeys("48408");
        System.out.println("端口修改完成");
    }

    public void subscription(){
        System.out.println("订阅初始化完成");
    }

    public void securityStrategy() throws InterruptedException {
        JavascriptExecutor js2 = (JavascriptExecutor) driver;

        String security = "[id$='-OpcUaSecurityPolicyGroup'] " +
                "th[column_key='selected'] .checkboxCols input[type='checkbox']";

        Object result = js2.executeScript(
                "const sel = arguments[0];" +
                        "const el = document.querySelector(sel);" +
                        "if (!el) return 'NOT_FOUND';" +
                        "el.scrollIntoView({block:'center', behavior:'instant'});" +  // 滚到视口中间
                        "el.click();" +                                              // 点击
                        "return el.checked ? 'CHECKED' : 'UNCHECKED';",
                security
        );
        //因为async，所以unchecked
        System.out.println("结果: " + result);

        Thread.sleep(5000);
    }

    public void certification(){
        System.out.println("证书配置完成");
    }

    public void userAdministration() throws InterruptedException {
        //add user
        JavascriptExecutor js = (JavascriptExecutor) driver;
        By ICON = By.xpath(
                "//div[contains(@class,'section-header')]" +
                        "[.//div[contains(@class,'section-title') and normalize-space(.)='用户管理']]" +
                        "/following-sibling::*[contains(@class,'setting-container')]" +
                        "//span[contains(@class,'icon-add')]");

        WebElement icon = driver.findElement(ICON);
        icon.click();

        JavascriptExecutor userjs = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// 1. 等弹窗出现
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".dialogBlock")));

// 2. 填三个框
        String setValueFn =
                "function setValue(root, labelText, val, type) {" +
                        "  const label = [...root.querySelectorAll('label.label')]" +
                        "      .find(l => l.querySelector('div')" +
                        "          && l.querySelector('div').textContent.trim() === labelText);" +
                        "  if (!label) return 'NO_LABEL:' + labelText;" +
                        "  let input = type ? label.querySelector('input[type=\"' + type + '\"]')" +
                        "                   : label.querySelector('input');" +
                        "  if (!input) return 'NO_INPUT:' + labelText;" +
                        "  const setter = Object.getOwnPropertyDescriptor(" +
                        "      window.HTMLInputElement.prototype, 'value').set;" +
                        "  setter.call(input, val);" +
                        "  input.dispatchEvent(new Event('input', {bubbles:true}));" +
                        "  input.dispatchEvent(new Event('change', {bubbles:true}));" +
                        "  return 'OK';" +
                        "}";

        String user = "testuser";
        String pwd  = "Abcd1234";

        System.out.println(userjs.executeScript(
                setValueFn +
                        "const d = document.querySelector('.dialogBlock');" +
                        "return d ? setValue(d, '用户名', arguments[0], null) : 'NO_DIALOG';",
                user));

        System.out.println(userjs.executeScript(
                setValueFn +
                        "const d = document.querySelector('.dialogBlock');" +
                        "return d ? setValue(d, '密码', arguments[0], 'password') : 'NO_DIALOG';",
                pwd));

        System.out.println(userjs.executeScript(
                setValueFn +
                        "const d = document.querySelector('.dialogBlock');" +
                        "return d ? setValue(d, '确认密码', arguments[0], 'password') : 'NO_DIALOG';",
                pwd));

        Thread.sleep(2000);

        // 3. 等确定可点 + 点击
        By OK_BTN = By.xpath(
                "//div[contains(@class,'dialogBlock')]" +
                        "//button[contains(@class,'theia-button') and contains(@class,'main')" +
                        " and normalize-space(.)='确定' and not(@disabled)]");

        WebElement okBtn = wait.until(ExpectedConditions.elementToBeClickable(OK_BTN));
        js.executeScript("arguments[0].click();", okBtn);
        System.out.println("已点确定");

        Thread.sleep(3000);
        System.out.println("用户管理完成");
    }
}
