package com.baoSight.DTO.model.execution;

import com.baoSight.service.caseAutoRunner.protocol.atomManipulation;
import lombok.Builder;
import lombok.Data;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

@Data
public class CaseExecutionResult {
    private com.baoSight.DTO.model.execution.opcua.OpcUaCaseData opcUaCaseData;



}
