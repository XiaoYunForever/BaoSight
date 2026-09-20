package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.service.caseAutoRunner.protocol.atomManipulation;
import org.openqa.selenium.WebDriver;

public class opcua_atom extends atomManipulation {
    public opcua_atom(WebDriver driver) {
        super(driver); // 继承父类的driver
    }

    public void opcuaInitial(){
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
