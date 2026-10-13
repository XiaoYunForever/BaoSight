package com.baoSight.service.evaluation;

import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;

public abstract  class AbstractEvaluation implements Evaluation {

    @Override
    public CaseEvaluationResult evaluate(CaseAnalysisResult caseAnalysisResult) {
        return null;
    }


}
