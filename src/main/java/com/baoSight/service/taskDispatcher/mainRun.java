package com.baoSight.service.taskDispatcher;

import com.baoSight.DTO.UserTestDTO;
import com.baoSight.service.caseAutoRunner.protocol.Protocol;
import com.baoSight.service.evaluation.Evaluation;
import com.baoSight.service.taskDispatcher.dataProcess.Dataprocess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class mainRun {
    ArrayList<String> codelist;
    String code;
    Character protocolNum;
    String caseNum;

    String protocolName;
    String caseName;

    @Autowired
    private ApplicationContext applicationContext;


    public void run(UserTestDTO usertestdto) {
        codelist = usertestdto.getCodelist();



        //解析
        for(int i=0;i<codelist.size();i++){
            code = codelist.get(i);
            protocolNum = code.charAt(0);
            caseNum = code.substring(1);

            if(protocolNum == '6'){
                //OPCUA
                protocolName = "opcua";
            }
            caseName = protocolName+"_"+caseNum;

            //自动化模块反射执行
            Protocol protocol = applicationContext.getBean("case"+"_"+caseName,Protocol.class);
            protocol.start();

//            //数据解析处理
//            Dataprocess dataprocess = applicationContext.getBean("dataprocess"+"_"+caseName,Dataprocess.class);
//            dataprocess.process(usertestdto);

            //简易评分
            Evaluation evaluation = applicationContext.getBean("evalu"+"_"+caseName,Evaluation.class);
            evaluation.evaluate();
            //
        }


        //总评分模块
//        score1*(5/5+9+7+5)+

        //报告生成
        //RESULT
        //return RESULT
    }
}
