package com.baoSight.DTO.model.execution;

import com.baoSight.DTO.model.common.DeviceInfo;
import com.baoSight.enums.ExecutionMode;
import lombok.Data;

@Data
public class CaseExecutionRequest {
    private String caseCode;
//    private DeviceInfo device;
    private ExecutionMode mode;
    private String connectionProfileId;
}
