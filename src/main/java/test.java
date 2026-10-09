import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import java.util.List;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class test {
    public static void main(String[] args) throws InterruptedException, ExecutionException, UaException {

//        String endpointUrl = "opc.tcp://192.168.1.1:4840";
//        // 1. 发现端点
//        List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointUrl).get();
//        EndpointDescription endpoint = endpoints.get(0);
//        // 2. 配置客户端
//        OpcUaClientConfig config = OpcUaClientConfig.builder()
//                .setApplicationName(LocalizedText.english("Example Client"))
////                .setApplicationUri("urn:example:client")
//                .setEndpoint(endpoint)
//                .setRequestTimeout(uint(5000))
//                .build();
//        // 3. 创建并连接
//        OpcUaClient client = OpcUaClient.create(config);
//        client.connect().get();
//        System.out.println("Connected successfully");
//        // 4. 断开连接
//        client.disconnect().get();





        //打开IDE
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");        // 禁用通知弹窗
        options.addArguments("--disable-geolocation");          // 禁用定位
        options.addArguments("--use-fake-ui-for-media-stream"); // 自动允许摄像头/麦克风
        options.addArguments("--deny-permission-prompts");
        options.addArguments("--user-agent=MyAgent");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
        driver.manage().window().maximize();
        driver.get("https://localhost:5571");
//        Thread.sleep(20000);
        //登录 Service版本
//        WebElement username = driver.findElement(By.id("username"));
//        username.sendKeys("admin");
//        Thread.sleep(2000);
//        WebElement password = driver.findElement(By.id("password"));
//        password.sendKeys("admin123");
//        Thread.sleep(2000);
//        password.sendKeys(Keys.ENTER);

        //delete
//        WebElement oldproject = driver.findElement(By.xpath("//li[contains(@class,'p-MenuBar-item') and .//div[normalize-space()='项目']]"));
//        oldproject.click();
//        WebElement oldproject2 = driver.findElement(By.xpath("//li[contains(@class,'p-Menu-item') and .//div[normalize-space()='删除项目']]"));
//        oldproject2.click();
//        WebElement oldproject3 = driver.findElement(By.xpath("/html/body/div[9]/div/div[2]/div/div[1]/div/table/thead/tr/th[1]/div/div[1]/input"));
//        oldproject3.click();
//        WebElement oldproject4 = driver.findElement(By.cssSelector("button.projectr-delete-dialog-control-accept-button"));
//        oldproject4.click();
//        WebElement oldproject5 = driver.findElement(By.cssSelector("button.common-dialog-accept-button"));
//        oldproject5.click();
//        WebElement oldproject6 = driver.findElement(By.cssSelector("button.projectr-delete-dialog-control-close-button"));
//        oldproject6.click();

        //创建项目
        WebElement newproject = driver.findElement(By.className("start-up-new-project"));
        newproject.click();
        WebElement projectname = driver.findElement(By.xpath("/html/body/div[7]/div/div[2]/div/label[1]/div[2]/input"));
        projectname.sendKeys("aqwtqedsstqe");

        //project path
        WebElement path = driver.findElement(By.xpath("/html/body/div[7]/div/div[2]/div/div[2]/div[2]/div"));
        path.click();
        Thread.sleep(2000);
        WebElement path2 = driver.findElement(By.xpath("/html/body/div[8]/div/div[2]/div[2]/div[1]/div[1]/div/div/div/div/div/div/div[3]"));
        path2.click();
        WebElement path3 = driver.findElement(By.xpath("/html/body/div[8]/div/div[3]/button[1]"));
        path3.click();


        WebElement author = driver.findElement(By.xpath("/html/body/div[7]/div/div[2]/div/label[2]/div[2]/input"));
        author.sendKeys("admin");

        WebElement confirm = driver.findElement(By.xpath("/html/body/div[7]/div/div[3]/button[1]"));
        confirm.click();
        Thread.sleep(2000);
        //model select
        WebElement model = driver.findElement(By.cssSelector("button.device-creator-dialog-control-add-button"));
        model.click();




//==============================================================================================
        WebElement importVariable = driver.findElement(By.xpath("//*[normalize-space()='全局变量集']"));
        Actions a = new Actions(driver);
        a.contextClick(importVariable);
        Thread.sleep(5000);
        a.perform();

        WebElement importVariable2 = driver.findElement(By.className("p-Menu-itemLabel"));
        importVariable2.click();
        WebElement importVariable3 = driver.findElement(By.xpath("/html/body/div[9]/div/div[3]/button[1]"));
        importVariable3.click();

        //上传变量表
        WebElement fileupload = driver.findElement(By.cssSelector("div.iconsvg-import[test-id*='STD.GVS_1.gvs']"));
        fileupload.click();

        //显式等待
//        WebElement fileInput = new WebDriverWait(driver, Duration.ofSeconds(10))
//                .until(ExpectedConditions.presenceOfElementLocated(
//                        By.cssSelector("input[type='file'][accept='text/csv,.csv,text/text,.gvs']")));

        WebElement fileInput = driver.findElement(By.cssSelector("input[type='file'][accept='text/csv,.csv,text/text,.gvs']"));
        fileInput.sendKeys("E:\\test.csv");

        Thread.sleep(8000);
//        WebElement fileInput2 = driver.findElement(By.xpath("//button[text()='导入']"));
//        fileInput2.click();


        //单击并回到设备属性
        WebElement test = driver.findElement(By.xpath(
                "//div[contains(@class,'p-TabBar-tabLabel') and normalize-space(.)='PLC_1']"));
        test.click();
//==================================================================================================
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
            Thread.sleep(1000);
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

        //port:48400-48499
        By INPUT = By.cssSelector("input.baosky-common-number-input.property-input");
        WebElement opcport = driver.findElement(INPUT);

        opcport.clear();                          // 清空
        opcport.sendKeys("48408");                 // 输入新值

        //userConfig
        Thread.sleep(3000);
        //重置状态
        WebElement renew = driver.findElement(By.cssSelector(".property-tree-container [data-test-id='virtuoso-scroller']"));
        renew.click();

        Thread.sleep(3000);

        //安全策略
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



//        用户策略
        JavascriptExecutor js3 = (JavascriptExecutor) driver;

        String labelText = "开启OPC UA用户管理";
        Object user_result = js3.executeScript(
                "const label = [...document.querySelectorAll('.setting-label')]" +
                        "    .find(el => el.textContent.trim() === arguments[0]);" +
                        "if (!label) return 'NO_LABEL';" +
                        "const el = label.closest('.setting-item').querySelector('input.rc-checkbox-input');" +
                        "if (!el) return 'NO_INPUT';" +
                        "el.scrollIntoView({block:'center', behavior:'instant'});" +
                        "['mousedown','mouseup','click'].forEach(t => {" +
                        "  el.dispatchEvent(new MouseEvent(t, {bubbles:true, cancelable:true, view:window}));" +
                        "});" +
                        "return 'CLICKED';",
                labelText
        );

        System.out.println(user_result);
        Thread.sleep(300);

        // 单独读状态
        Boolean checked = (Boolean) js.executeScript(
                "const label = [...document.querySelectorAll('.setting-label')]" +
                        "    .find(el => el.textContent.trim() === arguments[0]);" +
                        "const el = label.closest('.setting-item').querySelector('input.rc-checkbox-input');" +
                        "return el.checked;",
                labelText
        );
        System.out.println("checked = " + checked);

        //add user
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

        //添加证书
        JavascriptExecutor js4 = (JavascriptExecutor) driver;

        String groupSel = "[id$='-OpcUaCertificateManagementGroup']";

        Object testresult = js4.executeScript(
                "const group = document.querySelector(arguments[0]);" +
                        "if (!group) return 'NO_GROUP';" +
                        "const el = group.querySelector('.icon-add');" +
                        "if (!el) return 'NO_ICON';" +
                        "if (el.classList.contains('disabled-icon')) return 'DISABLED';" +  // 禁用就别点了
                        "el.scrollIntoView({block:'center', behavior:'instant'});" +
                        "['mousedown','mouseup','click'].forEach(t => {" +
                        "  el.dispatchEvent(new MouseEvent(t, {bubbles:true, cancelable:true, view:window}));" +
                        "});" +
                        "return 'CLICKED';",
                groupSel
        );

        System.out.println(testresult);
        Thread.sleep(3000);
        WebElement cert = driver.findElement(By.xpath("//button[contains(@class,'theia-button') and normalize-space(.)='确定']"));
        cert.click();



        //cert_config
        //导入证书step1
        Thread.sleep(3000);
        JavascriptExecutor js5 = (JavascriptExecutor) driver;

        Object qresult = js5.executeScript(
                // 定位这一行的 select 容器
                "const sel = document.querySelector(" +
                        "  \"tr td[column_key='opcUaSecurityPolicyName'] .baosky-rc-select-container\"" +
                        ");" +
                        "if (!sel) return 'NO_SELECT';" +

                        // 找到内部的 rc-select-selector（真正接收点击的）
                        "const selector = sel.querySelector('.rc-select-selector');" +
                        "if (!selector) return 'NO_SELECTOR';" +

                        // 滚到中间
                        "selector.scrollIntoView({block:'center', behavior:'instant'});" +

                        // 模拟 mousedown 展开下拉框（rc-select 靠 mousedown 触发）
                        "selector.dispatchEvent(new MouseEvent('mousedown', " +
                        "  {bubbles:true, cancelable:true, view:window}));" +

                        "return 'OPENED';"
        );

        System.out.println(qresult);
        //导入证书step2：选择证书
        Thread.sleep(3000);
        WebElement certconfig2 = driver.findElement(By.xpath("//div[contains(@class,'rc-select-item-option-content') and normalize-space(.)='OPC_Cert_1']"));
        certconfig2.click();
    }
}
