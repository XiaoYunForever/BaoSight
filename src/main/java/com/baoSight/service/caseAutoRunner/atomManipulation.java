package com.baoSight.service.caseAutoRunner;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class atomManipulation {
    protected WebDriver driver;

    public void open() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-agent=MyAgent");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
        driver.get("https://localhost:5571");
    }

    public void close() {
        driver.quit();
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
        projectname.sendKeys("qqq");

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
        System.out.println("载入程序");
    }

    public void readCPUAndLoad(){
        System.out.println("读取CPU及通讯负载");
    }

    public void downloadToPLC(){
        System.out.println("PLC下载完成");

    }
}
