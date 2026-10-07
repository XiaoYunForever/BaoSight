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






    /**
     *  用java-SDK来模拟opcua手动操作客户端，由于本质是测试PLC端是否可以通过opcua通信测试，客户端侧是代码还是人为操作并不重要
     *  OPCUA人工操作只在OPCUA类下实现，而且本身数据需要通过opcua类传回给mainRun，比如前面原子操作获取的CPU负载，应该统一打包回传给mainRun
     *
     *
     *
     *
     * */

}
