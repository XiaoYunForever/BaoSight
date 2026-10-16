package com.baoSight.service.taskDispatcher;


import com.baoSight.DTO.api.request.CreateTestRequest;
import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
import com.baoSight.DTO.model.common.DeviceInfo;
import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;
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
    ArrayList<String> codelist;
    String code;
    Character protocolNum;
    String caseNum;

    ArrayList<CaseEvaluationResult>  caseEvaluationResultList;
    float finalScore;

    String protocolName;
    String caseName;

    @Autowired
    private ApplicationContext applicationContext;


    public void run(CreateTestRequest createTestRequest) throws Exception {
        codelist = createTestRequest.getCodelist();
        caseEvaluationResultList = new ArrayList<>();
        // 解析数据类
        CaseExecutionRequest caseExecutionRequest = new CaseExecutionRequest();
        caseExecutionRequest.setDevice(createTestRequest.getDevice());
        caseExecutionRequest.setMode(createTestRequest.getMode());// 把执行模式从前端传递下来

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
            CaseExecutionResult result = protocol.start(caseExecutionRequest);


            System.out.println("value " + result.getOpcUaCaseData().getServerTimeText());
            System.out.println("connected " + result.getOpcUaCaseData().isConnected());



            //简易评分

            Evaluation evaluation = applicationContext.getBean("evalu"+"_"+caseName,Evaluation.class);
            CaseEvaluationResult evalu_result = evaluation.evaluate(result);
            caseEvaluationResultList.add(evalu_result);
            //
        }


        //总评分模块
        evaluationAll evaluationAll = new evaluationAll();
        finalScore = evaluationAll.evaluate(caseEvaluationResultList);
        System.out.println("finalScore " + finalScore);

        //报告生成
        //RESULT
        //return RESULT
    }
}
