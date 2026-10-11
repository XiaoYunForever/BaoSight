package com.baoSight.DTO.api.request;

import lombok.Data;

@Data
public class DeviceRequest {
    private String manufacturer;
    private String model;
    private String version;
}
