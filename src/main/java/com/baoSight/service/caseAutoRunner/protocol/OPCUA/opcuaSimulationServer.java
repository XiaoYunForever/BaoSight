package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

public class opcuaSimulationServer {
    public void serverFactory(String num){
        if(num.equals("1")){
            System.out.println("Real_PLC_Test");

        }



        if(num.equals("2")){
            System.out.println("Sim_PLC_Test");
        }



    }

}
