package com.baoSight.service.caseAutoRunner.protocol;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.Objects;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class atomManipulation {
    // 项目页通过 URL 片段标识，域名和项目名可以变化。
    private static final String PROJECT_PAGE_MARKER = "#bsprojects:";
    /** 通用操作和协议操作共用本次用例的浏览器，原子操作不负责关闭它。 */
    //driver是浏览器控制器，实现原子操作，必须先通过session创建会话创建driver，通过对应的driver驱动原子操作
    protected final WebDriver driver;//

    public atomManipulation(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver 不能为空");
    }

    public void initialization(){
        System.out.println("初始化开始\n");
        //登录 Service版本
//        WebElement username = driver.findElement(By.id("username"));
//        username.sendKeys("admin");
//        WebElement password = driver.findElement(By.id("password"));
//        password.sendKeys("admin123");
//        password.sendKeys(Keys.ENTER);

        //创建项目
        WebElement newproject = driver.findElement(By.className("start-up-new-project"));
        newproject.click();
        WebElement projectname = driver.findElement(By.xpath("/html/body/div[7]/div/div[2]/div/label[1]/div[2]/input"));
        projectname.sendKeys("qqq56789121233");

        //project path
        WebElement path = driver.findElement(By.xpath("/html/body/div[7]/div/div[2]/div/div[2]/div[2]/div"));
        path.click();
        WebElement path2 = driver.findElement(By.xpath("/html/body/div[8]/div/div[2]/div[2]/div[1]/div[1]/div/div/div/div/div/div/div[3]"));
        path2.click();
        WebElement path3 = driver.findElement(By.xpath("/html/body/div[8]/div/div[3]/button[1]"));
        path3.click();


        WebElement author = driver.findElement(By.xpath("/html/body/div[7]/div/div[2]/div/label[2]/div[2]/input"));
        author.sendKeys("admin");

        WebElement confirm = driver.findElement(By.xpath("/html/body/div[7]/div/div[3]/button[1]"));
        confirm.click();
        //model select
        WebElement model = driver.findElement(By.xpath("/html/body/div[6]/div/div[3]/button[1]"));
        model.click();

        System.out.println("初始化完成");
    }

    public void loadingVriables(){
        System.out.println("载入变量表");
    }




    public void loadingPrograms(){
        ensureProjectPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // 1. 右键左侧树中的“程序单元”。
        WebElement programUnit = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[normalize-space()='程序单元']")));
        new Actions(driver).contextClick(programUnit).perform();

        // 2. 在右键菜单中选择“添加程序单元”。
        clickVisible(wait,"添加程序单元");

        // 3. 等待“添加程序单元模块”对话框出现，并打开实现语言下拉框。
        clickVisible(wait,"梯形逻辑图（LD）");

        // 4. 选择结构化文本 ST。
        clickVisible(wait,"结构化文本（ST）");

        // 5. 确定创建程序单元。
        clickVisible(wait,"确定");

    }






    public void readCPUAndLoad(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        String programLoad = readPercentAfterLabel(wait, "程序负载");
        String communicationLoad = readPercentAfterLabel(wait, "通讯负载");

        System.out.println("程序负载：" + programLoad);
        System.out.println("通信负载：" + communicationLoad);
    }

    /** 读取指定负载标题之后的第一个百分比文本。 */
    private String readPercentAfterLabel(WebDriverWait wait, String label) {
        WebElement value = wait.until(d -> {
            String xpath = "//*[normalize-space()='" + label + "']"
                    + "/following::*[contains(normalize-space(), '%')][1]";
            for (WebElement candidate : d.findElements(By.xpath(xpath))) {
                if (candidate.isDisplayed()) return candidate;
            }
            return null;
        });

        Matcher matcher = Pattern.compile("\\b\\d+(?:\\.\\d+)?%")
                .matcher(value.getText());
        if (!matcher.find()) {
            throw new IllegalStateException("未能从“" + label + "”读取百分比：" + value.getText());
        }
        return matcher.group();
    }

    public void downloadToPLC(){
        // 包括在线+下载两个步骤 ,暂时先用sim方法替代，没有实物可以测试
        System.out.println("PLC下载完成");

    }

    public void simAndDownload() {
        ensureProjectPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        // 在线 -> 启动仿真
        clickVisible(wait, "在线");
        clickVisible(wait, "启动仿真");

        // 先等待“PLC在线中，请稍等”提示出现，再等待提示消失。
        // 提示消失后才认为仿真在线完成，继续执行下载。
//        By onlineLoading = By.xpath(
//                "//*[contains(normalize-space(),'PLC在线中')]"
//        );
//        wait.until(ExpectedConditions.visibilityOfElementLocated(onlineLoading));
//        wait.until(ExpectedConditions.invisibilityOfElementLocated(onlineLoading));
//        System.out.println("仿真已在线。");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        // 在线 -> 下载到设备
        clickVisible(wait, "在线");
        clickVisible(wait, "下载到设备");

        // 下载可能出现进度窗口；若出现“一致，无需下载”提示则关闭它。
        WebDriverWait resultWait = new WebDriverWait(driver, Duration.ofSeconds(60));
        try {
            WebElement sameNotice = resultWait.until(d -> {
                for (WebElement element : d.findElements(
                        By.xpath("//*[contains(normalize-space(),'上下位机一致') and contains(normalize-space(),'无需下载') ]"))) {
                    if (element.isDisplayed()) return element;
                }
                return null;
            });
            By resultDialog = By.id("theia-dialog-shell");
            By closeButton = By.cssSelector(
                    "#theia-dialog-shell .dialogControl button.theia-button.main"
            );
            WebElement close = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(closeButton));
            try {
                close.click();
            } catch (org.openqa.selenium.ElementClickInterceptedException e) {
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", close);
            }
            resultWait.until(ExpectedConditions.invisibilityOfElementLocated(resultDialog));
            System.out.println("检测到上下位机一致提示，已关闭提示框。");
        } catch (org.openqa.selenium.TimeoutException ignored) {
            // 未出现该提示，保留其他下载结果，不把结果写死为“一致”。
            System.out.println("未出现上下位机一致提示，下载流程已继续。");
        }
    }

    /*
        上面写原子操作，下面写原子操作需要使用的私有函数
        --------------------------
        通用方法，私有方法
        ensureProjectPage()：保证在当前PLC项目页面
        clickVisible(): 显式等待，判断什么时候可以点击
     */

    private void ensureProjectPage() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl == null || currentUrl.isBlank()
                || !currentUrl.contains(PROJECT_PAGE_MARKER)) {
            throw new IllegalStateException("当前不在 PLC 项目页面：" + currentUrl);
        }
    }

    private void clickVisible(WebDriverWait wait, String text) {
        // text 是页面上显示的完整文字，例如“在线”或“启动仿真”。
        By textLocator = By.xpath("//*[normalize-space()='" + text + "']");
        WebElement element = wait.until(d -> {
            for (WebElement candidate : d.findElements(textLocator)) {
                if (candidate.isDisplayed() && candidate.isEnabled()) return candidate;
            }
            return null;
        });
        element.click();
        System.out.println(text+"完成");
    }
    

}
