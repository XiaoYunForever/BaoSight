package com.baoSight.service.taskDispatcher.dataProcess;

import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
import com.baoSight.DTO.model.execution.CaseExecutionResult;

public interface Dataprocess {

    public CaseAnalysisResult process(CaseExecutionResult rawData);

}
