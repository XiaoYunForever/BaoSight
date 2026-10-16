package com.baoSight.service.evaluation;

import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;
import com.baoSight.DTO.model.execution.CaseExecutionResult;

public abstract  class AbstractEvaluation implements Evaluation {

    @Override
    public CaseEvaluationResult evaluate(CaseExecutionResult caseExecutionResult) {
        return null;
    }


}
