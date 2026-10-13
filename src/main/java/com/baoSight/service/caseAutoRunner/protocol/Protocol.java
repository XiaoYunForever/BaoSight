package com.baoSight.service.caseAutoRunner.protocol;


import com.baoSight.DTO.model.execution.CaseExecutionRequest;
import com.baoSight.DTO.model.execution.CaseExecutionResult;
import com.baoSight.DTO.model.execution.opcua.OpcUaCaseData;

public interface Protocol {
    public CaseExecutionResult start(CaseExecutionRequest caseExecutionRequest) throws Exception;
    public OpcUaCaseData opcuaDataBuilder(CaseExecutionRequest caseExecutionRequest);
}
