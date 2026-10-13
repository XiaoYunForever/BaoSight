package com.baoSight.service.evaluation.OPCUA;

import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;
import com.baoSight.enums.ExecutionMode;
import com.baoSight.enums.Verdict;
import com.baoSight.service.evaluation.Evaluation;
import org.springframework.stereotype.Service;

@Service("evalu_opcua_01")
public class opcua_01 implements Evaluation {
    public CaseEvaluationResult  caseEvaluationResult;

    @Override
    public CaseEvaluationResult evaluate(CaseAnalysisResult caseAnalysisResult) {
        caseEvaluationResult.setMaxScore(20);
        caseEvaluationResult.setScore(20);
        caseEvaluationResult.setCaseCode(caseAnalysisResult.getCaseCode());

        if(caseAnalysisResult.getVerdict().equals(Verdict.FAIL)){
            caseEvaluationResult.setScore(caseEvaluationResult.getScore()-20);
            return caseEvaluationResult;
        }

        if(caseAnalysisResult.getResult().get("Anonymous").equals("Fail")){
            caseEvaluationResult.setScore(caseEvaluationResult.getScore()-10);
        }
        if(caseAnalysisResult.getResult().get("UserName").equals("Fail")){
            caseEvaluationResult.setScore(caseEvaluationResult.getScore()-10);
        }

        return caseEvaluationResult;
    }
}
