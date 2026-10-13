package com.baoSight.DTO.api.request;

import com.baoSight.DTO.model.common.DeviceInfo;
import com.baoSight.enums.ExecutionMode;
import lombok.Data;

import java.util.ArrayList;

@Data
public class CreateTestRequest {
    // 测试案例编号
    private ArrayList<String> codelist;
    // 设备信息
    private DeviceInfo device;
    //PLC or Simulation
    private ExecutionMode mode;
}
