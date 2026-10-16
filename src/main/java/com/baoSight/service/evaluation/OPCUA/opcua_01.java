package com.baoSight.service.evaluation.OPCUA;

import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;
import com.baoSight.DTO.model.execution.CaseExecutionResult;
import com.baoSight.enums.ExecutionMode;
import com.baoSight.enums.Verdict;
import com.baoSight.service.evaluation.Evaluation;
import org.springframework.stereotype.Service;

@Service("evalu_opcua_01")
public class opcua_01 implements Evaluation {
    public CaseEvaluationResult  caseEvaluationResult = new CaseEvaluationResult();

    @Override
    public CaseEvaluationResult evaluate(CaseExecutionResult caseExecutionResult) {
        caseEvaluationResult.setMaxScore(20);
        caseEvaluationResult.setScore(20);

        if(!caseExecutionResult.getOpcUaCaseData().isConnected()){
            caseEvaluationResult.setScore(caseEvaluationResult.getScore()-10);
            return caseEvaluationResult;
        }

        return caseEvaluationResult;
    }
}
