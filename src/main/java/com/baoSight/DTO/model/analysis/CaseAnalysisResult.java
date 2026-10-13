package com.baoSight.DTO.model.analysis;

import com.baoSight.enums.Verdict;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CaseAnalysisResult {
    private String casecode;
    private Verdict verdict;
    private Map<String,String> result;

}
