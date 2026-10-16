package com.baoSight.DTO.model.execution.opcua;

import com.baoSight.enums.ExecutionMode;
import com.baoSight.service.caseAutoRunner.protocol.atomManipulation;
import lombok.Data;

@Data
public class OpcUaCaseData {
    private boolean connected;
    private String serverTimeText;
    private String caseCode;
    private String errorText;
}
