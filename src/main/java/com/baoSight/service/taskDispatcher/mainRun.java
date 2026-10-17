package com.baoSight.service.taskDispatcher;


import com.baoSight.DTO.api.request.CreateTestRequest;
import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;
import com.baoSight.DTO.model.evaluation.TestEvaluationResult;
import com.baoSight.DTO.model.execution.CaseExecutionRequest;
import com.baoSight.DTO.model.execution.CaseExecutionResult;
import com.baoSight.enums.ExecutionMode;
import com.baoSight.service.caseAutoRunner.protocol.Protocol;
import com.baoSight.service.evaluation.Evaluation;
import com.baoSight.service.evaluation.evaluationAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class mainRun {

    @Autowired
    private ApplicationContext applicationContext;


    public TestEvaluationResult run(CreateTestRequest createTestRequest) throws Exception {
        ArrayList<String> codelist = createTestRequest.getCodelist();
        ArrayList<CaseEvaluationResult> caseEvaluationResultList = new ArrayList<>();
        // 解析数据类
        CaseExecutionRequest caseExecutionRequest = new CaseExecutionRequest();
//        caseExecutionRequest.setDevice(createTestRequest.getDevice());
        caseExecutionRequest.setMode(createTestRequest.getMode());// 把执行模式从前端传递下来

        //解析
        for(int i=0;i<codelist.size();i++){
            String code = codelist.get(i);
            char protocolNum = code.charAt(0);
            String caseNum = code.substring(1);
            String protocolName;

            if(protocolNum == '6'){
                //OPCUA
                protocolName = "opcua";
            } else {
                throw new IllegalArgumentException("尚未配置用例协议编号：" + code);
            }
            String caseName = protocolName+"_"+caseNum;
            caseExecutionRequest.setCaseCode(code);

            //自动化模块反射执行
            Protocol protocol = applicationContext.getBean("case"+"_"+caseName,Protocol.class);
            CaseExecutionResult result = protocol.start(caseExecutionRequest);
            //简易评分
            Evaluation evaluation = applicationContext.getBean("evalu"+"_"+caseName,Evaluation.class);
            CaseEvaluationResult evalu_result = evaluation.evaluate(result);
            caseEvaluationResultList.add(evalu_result);
            //
        }


        //总评分模块
        evaluationAll evaluationAll = new evaluationAll();
        TestEvaluationResult report = evaluationAll.summarize(caseEvaluationResultList);
        System.out.println("finalScore " + report.getFinalScore());
        System.out.println(report);
        return report;

    }
}
