package com.baoSight.DTO.model.execution.opcua;

import com.baoSight.enums.ExecutionMode;
import com.baoSight.service.caseAutoRunner.protocol.atomManipulation;
import lombok.Data;

@Data
public class OpcUaRequestData {
    private String caseName;
    private atomManipulation actions;
    private ExecutionMode mode;
    private String certMode;
    private String username;
    private String password;
}
