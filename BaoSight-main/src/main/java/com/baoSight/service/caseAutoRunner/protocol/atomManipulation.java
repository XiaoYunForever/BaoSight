package com.baoSight.service.caseAutoRunner.protocol;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.Objects;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public class atomManipulation implements atomManipulationInterface {
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
        projectname.sendKeys("aqq15");

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


    public void loadingPrograms(String caseName){
        ensureProjectPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        Path xmlFile = findPlcOpenXml(caseName);
        By importDialogLocator = By.cssSelector(".general-import-dialog");

        // 1. 项目 -> 导入外部文件。
        clickVisible(wait, "项目");
        clickVisible(wait, "导入外部文件");

        // 2. 在“导入外部文件”弹窗中选择 PLCopen 格式。
        clickDialogVisible(wait, importDialogLocator, "BSDL格式");
        clickDialogVisible(wait, importDialogLocator, "PLCopen格式");

        // 3. 选择 XML 文件并点击导入。
        WebElement importDialog = visibleDialog(wait, importDialogLocator);
        WebElement fileInput = importDialog.findElement(By.cssSelector("input[type='file']"));
        fileInput.sendKeys(xmlFile.toAbsolutePath().toString());
        clickDialogVisible(wait, importDialogLocator, "导入");

        // 导入前可能出现“需要关闭并保存相关窗口”的业务提示。
        // 该提示使用自定义弹窗容器，不是 #theia-dialog-shell 遮罩层。
        By closeWindowPromptLocator = By.cssSelector("#theia-dialog-shell");
        clickDialogVisible(wait, closeWindowPromptLocator, "确定");

//        // 4. 导入预览：将动作统一设为“覆盖全部”。
//        clickDialogVisible(wait, importDialogLocator, "跳过全部", "全部跳过");
//        clickDialogVisible(wait, importDialogLocator, "覆盖全部");
//        clickDialogVisible(wait, importDialogLocator, "确定");

        // 5. 导入完成后关闭结果弹窗。.dialog-mask-layer

        clickDialogVisible(wait, importDialogLocator, "关闭");
        System.out.println("PLCopen 文件导入完成：" + xmlFile);

        // 程序导入完成后紧接着创建任务并添加程序调用。
        addTask();
    }


    private void addTask(){
        ensureProjectPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // 任务节点的右键菜单中选择“添加任务”。
        rightClickVisible(wait, "任务");
        clickVisible(wait, "添加任务");

        // 确认任务参数（默认名称和“定期循环任务”沿用界面默认值）。
        By taskDialogLocator = By.id("theia-dialog-shell");
        clickDialogVisible(wait, taskDialogLocator, "确定");
        waitForInvisible(wait, taskDialogLocator);

        // 进入新建任务页面，添加程序调用。
        By callDialogLocator = By.id("theia-dialog-shell");
        By callDialogTitle = By.xpath("//*[@id='theia-dialog-shell']//*[normalize-space()='添加调用']");
        clickUntilVisible(wait, "添加调用", callDialogTitle);

        // 在“添加调用”弹窗中选中 STD.PU_1，然后确认。
        selectDialogItem(wait, callDialogLocator, "STD.PU_1");
        clickDialogVisible(wait, callDialogLocator, "确定");



        System.out.println("任务及 STD.PU_1 调用添加完成");
    }



    public void readCPUAndLoad(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        String programLoad = readPercentAfterLabel(wait, "程序负载");
        String communicationLoad = readPercentAfterLabel(wait, "通讯负载");

        System.out.println("程序负载：" + programLoad);
        System.out.println("通信负载：" + communicationLoad);
    }

    public void downloadToPLC(){
        // 包括在线+下载两个步骤 ,暂时先用sim方法替代，没有实物可以测试
        System.out.println("PLC下载完成");

    }

    public void simAndDownload() {
        ensureProjectPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        // 在线 -> 启动仿真
        clickVisible(wait, "在线");
        clickVisible(wait, "启动仿真");
        //等待在线过程结束
        waitForInvisible(wait, By.cssSelector("#theia-dialog-shell.loading-dialog"));
        System.out.println("仿真已在线。");
//----------------------------------------------------
        // 在线 -> 下载到设备
        clickVisible(wait, "在线");
        clickVisible(wait, "下载到设备");
        // 实际连接PLC的后续操作可能不同，到时候具体改，写到downloadToPLC就行。

        //点击下载或关闭弹窗，他们共用同一个cssSelector
        WebElement close = findDialogElement(wait, By.cssSelector(".dialogControl button.theia-button.main"));
        close.click();
        waitForInvisible(wait, By.id("theia-dialog-shell"));

    }

    /*
        上面写原子操作，下面写原子操作需要使用的私有函数
        --------------------------
        通用方法，私有方法
        ensureProjectPage()：保证在当前PLC项目页面
        clickVisible(): 显式等待，判断什么时候可以点击
     */
    // 保证在当前PLC项目页面
    private void ensureProjectPage() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl == null || currentUrl.isBlank()
                || !currentUrl.contains(PROJECT_PAGE_MARKER)) {
            throw new IllegalStateException("当前不在 PLC 项目页面：" + currentUrl);
        }
    }
    // 普通按键点击
    private void clickVisible(WebDriverWait wait, String text) {
        WebElement element = findVisibleElement(wait, text);
        element.click();
        System.out.println(text+"完成");
        sleep(1000);/** 点击结束睡一秒等待一下*/


    }
    // 普通右键点击
    private void rightClickVisible(WebDriverWait wait, String text) {
        WebElement element = findVisibleElement(wait, text);
        new Actions(driver).contextClick(element).perform();
        System.out.println(text + "右键完成");
        sleep(1000);/** 点击结束睡一秒等待一下*/
    }

    // 查找可点击的按键
    private WebElement findVisibleElement(WebDriverWait wait, String text) {
        By textLocator = By.xpath("//*[normalize-space()='" + text + "']");
        return findVisibleElement(wait, textLocator);
    }

    /** 按 CSS/XPath 等定位器查找可见且可操作的元素。 */
    private WebElement findVisibleElement(WebDriverWait wait, By locator) {
        return wait.until(d -> {
            for (WebElement candidate : d.findElements(locator)) {
                if (candidate.isDisplayed() && candidate.isEnabled()) {
                    return candidate;
                }
            }
            return null;
        });
    }

    /** 点击后等待目标出现；已出现就不再点击，未出现才重试。 */
    private void clickUntilVisible(WebDriverWait wait, String text, By resultLocator) {
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        try {
            WebDriverWait resultWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            for (int attempt = 1; attempt <= 3; attempt++) {
                if (ExpectedConditions.visibilityOfElementLocated(resultLocator).apply(driver) != null) return;
                clickVisible(wait, text);
                try {
                    resultWait.until(ExpectedConditions.visibilityOfElementLocated(resultLocator));
                    return;
                } catch (TimeoutException ignored) { /* 未出现时再点击一次。 */ }
            }
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
        }
        throw new IllegalStateException("点击“" + text + "”后，目标仍未出现：" + resultLocator);
    }

    /**
    * 路径分为基础路径+子路径，基础路径在properties，子路径是由调用的具体案例而定
    * 比如opcua1，需要传入opcua01参数，定位到具体的目录
    * */
    private Path findPlcOpenXml(String caseName) {
        if (caseName == null || caseName.isBlank()) {
            throw new IllegalArgumentException("caseName 不能为空");
        }
        Properties properties = loadImportProperties();

        String configuredPath = properties.getProperty("plc.import.path");
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalStateException("未配置 plc.import.path");
        }

        Path directory = resolveImportDirectory(configuredPath.trim(), caseName.trim());
        if (!Files.isDirectory(directory)) {
            throw new IllegalStateException("导入目录不存在或不是目录：" + directory);
        }
        try (java.util.stream.Stream<Path> files = Files.walk(directory, 1)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT).endsWith(".xml"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "用例目录中未找到 PLCopen XML 文件：" + directory));
        } catch (IOException e) {
            throw new IllegalStateException("无法读取 PLCopen 文件目录：" + directory, e);
        }
    }

    /**
     * 读取配置：外部文件优先，classpath 中的默认配置兜底。
     * 可通过 -Dplc.config.file=... 指定外部配置文件。
     */
    private Properties loadImportProperties() {
        Properties properties = new Properties();
        String externalConfig = System.getProperty("plc.config.file");
        if (externalConfig == null || externalConfig.isBlank()) {
            externalConfig = System.getenv("PLC_CONFIG_FILE");
        }

        Path defaultExternal = Path.of("config", "application.properties");
        Path externalPath = externalConfig == null || externalConfig.isBlank()
                ? defaultExternal
                : Path.of(externalConfig.trim());

        if (Files.isRegularFile(externalPath)) {
            try (InputStream input = Files.newInputStream(externalPath)) {
                properties.load(input);
                return properties;
            } catch (IOException e) {
                throw new IllegalStateException("读取外部配置失败：" + externalPath, e);
            }
        }

        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("未找到 application.properties");
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("读取 application.properties 失败", e);
        }
    }

    /** 解析 classpath:... 或 Windows/Linux 文件系统路径。 */
    private Path resolveImportDirectory(String configuredPath, String caseName) {
        if (!configuredPath.startsWith("classpath:")) {
            return Path.of(configuredPath).resolve(caseName);
        }

        String resourcePath = configuredPath.substring("classpath:".length())
                .replaceFirst("^[/\\\\]+", "");
        String caseResourcePath = resourcePath + "/" + caseName;
        URL resource = getClass().getClassLoader().getResource(caseResourcePath);
        if (resource == null) {
            throw new IllegalStateException("找不到 classpath 导入目录：" + caseResourcePath);
        }
        try {
            // 仅 file: 资源可直接作为 Selenium file input 所需的本地路径。
            if (!"file".equalsIgnoreCase(resource.getProtocol())) {
                throw new IllegalStateException(
                        "classpath 资源位于压缩包内，无法直接作为文件上传；请将 plc.import.path 配置为外部磁盘目录："
                                + caseResourcePath);
            }
            return Path.of(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("classpath 导入目录路径无效：" + caseResourcePath, e);
        }
    }

    /** 返回当前显示的 Theia 弹窗。 */
    private WebElement visibleDialog(WebDriverWait wait, By dialogLocator) {
        return wait.until(d -> {
            for (WebElement dialog : d.findElements(dialogLocator)) {
                if (dialog.isDisplayed()) return dialog;
            }
            return null;
        });
    }

    /** 只在当前弹窗内按文本点击，避免误点背景页面的同名控件。 */
    private void clickDialogVisible(WebDriverWait wait, /*弹窗的定位器*/By dialogLocator, String text) {
        WebElement element = findDialogElement(
                wait,
                dialogLocator,
                By.xpath(".//*[normalize-space()='" + text + "']"));
        String clickedText = element.getText();
        element.click();
        // 点击后弹窗可能立即刷新，不能再读取原 WebElement 的属性。
        System.out.println(clickedText + "完成");
        sleep(1000);/** 点击结束睡一秒等待一下*/
    }

    /** 在弹窗中选择列表项；优先点击该行的复选框，找不到复选框时再点击文本本身。 */
    private void selectDialogItem(WebDriverWait wait, By dialogLocator, String text) {
        WebElement textElement = findDialogElement(
                wait,
                dialogLocator,
                By.xpath(".//*[normalize-space()='" + text + "']"));

        // 添加调用弹窗使用复选框选择程序单元。通过文本元素向上查找最近的复选框容器，
        // 避免点击到弹窗背景或同名的其他控件。
        for (WebElement container : textElement.findElements(By.xpath(
                "./ancestor::*[.//input[@type='checkbox'] or .//*[@role='checkbox']][1]"))) {
            for (WebElement checkbox : container.findElements(By.cssSelector(
                    "input[type='checkbox'], [role='checkbox']"))) {
                if (checkbox.isDisplayed() && checkbox.isEnabled()) {
                    checkbox.click();
                    System.out.println(text + "选中完成");
                    sleep(1000);
                    return;
                }
            }
        }

        // 某些版本把整行设为可点击，保留文本点击作为兼容路径。
        textElement.click();
        System.out.println(text + "选中完成");
        sleep(1000);
    }

    //不填第二个参数默认弹窗定位器为"theia-dialog-shell"
    private WebElement findDialogElement(WebDriverWait wait, By childLocator) {
        return findDialogElement(wait, By.id("theia-dialog-shell"), childLocator);
    }

    /** 在调用方指定的弹窗容器内查找可见且可操作的元素。 */
    private WebElement findDialogElement(
            WebDriverWait wait, By dialogLocator, By childLocator) {
        return wait.until(d -> {
            for (WebElement dialog : d.findElements(dialogLocator)) {
                if (!dialog.isDisplayed()) continue;
                for (WebElement element : dialog.findElements(childLocator)) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        return element;
                    }
                }
            }
            return null;
        });
    }

    /** 等待元素消失 */
    private void waitForInvisible(WebDriverWait wait, By locator) {
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
        }
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

    private void sleep(long millis) {
        try{
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    

}
