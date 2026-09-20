package com.baoSight.service.caseAutoRunner.protocol;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.Objects;
import java.time.Duration;

public class atomManipulation {
    // 域名未知时只校验页面路径后缀；实际页面路径确定后修改这一处即可。
    private static final String EXPECTED_PAGE_SUFFIX = "Baosky IDE2.html";
    /** 通用操作和协议操作共用本次用例的浏览器，原子操作不负责关闭它。 */
    //driver是浏览器控制器，实现原子操作，必须先通过session创建会话创建driver，通过对应的driver驱动原子操作
    protected final WebDriver driver;//

    public atomManipulation(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver 不能为空");
    }

    public void initialization(){
        System.out.println("初始化完成");
    }

    public void loadingVriables(){
        System.out.println("载入变量表");
    }




    public void loadingPrograms(){

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement programUnit = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[normalize-space()='程序单元']")));
        programUnit.click();
        System.out.println("已进入程序单元页面。");
    }

    public void readCPUAndLoad(){
        System.out.println("读取CPU及通讯负载");
    }

    public void downloadToPLC(){
        System.out.println("PLC下载完成");

    }
}
