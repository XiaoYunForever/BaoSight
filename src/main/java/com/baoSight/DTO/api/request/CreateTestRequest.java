package com.baoSight.DTO.api.request;

import com.baoSight.enums.ExecutionMode;
import lombok.Data;

import java.util.ArrayList;

@Data
public class CreateTestRequest {
    private ArrayList<String> codelist;
    private DeviceRequest device;
    private ExecutionMode mode;
}
