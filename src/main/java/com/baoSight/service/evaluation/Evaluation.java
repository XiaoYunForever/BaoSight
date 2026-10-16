package com.baoSight.service.evaluation;

import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;
import com.baoSight.DTO.model.execution.CaseExecutionResult;

public interface Evaluation {
    public CaseEvaluationResult evaluate(CaseExecutionResult caseExecutionResult);
}
