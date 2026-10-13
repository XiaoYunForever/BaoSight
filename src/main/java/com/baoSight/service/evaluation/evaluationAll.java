package com.baoSight.service.evaluation;

import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;

import java.math.BigDecimal;
import java.util.ArrayList;


public class evaluationAll{
    private int totalScore;
    private int MaxScore;
    private float finalScore;

    public float evaluate(ArrayList<CaseEvaluationResult> caseEvaluationResultList) {
        MaxScore = 0;
        for(CaseEvaluationResult caseEvaluationResult:caseEvaluationResultList){
            MaxScore = MaxScore + caseEvaluationResult.getMaxScore();
        }
        totalScore = 0;
        for(CaseEvaluationResult caseEvaluationResult:caseEvaluationResultList){
            totalScore = totalScore + caseEvaluationResult.getScore();
        }
        finalScore = ((float) totalScore / (float) MaxScore) * 100;

        return finalScore;
    }
}
