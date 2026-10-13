package com.baoSight.service.evaluation;

import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;

public interface Evaluation {
    public CaseEvaluationResult evaluate(CaseAnalysisResult  caseAnalysisResult);
}
