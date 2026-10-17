package com.baoSight.DTO.model.evaluation;

import lombok.Data;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 一次完整测试的结果，包含逐用例记录、协议汇总及总分。 */
@Data
public class TestEvaluationResult {
    //private List<CaseEvaluationResult> caseResults = new ArrayList<>();
    private Map<String, ProtocolEvaluationSummary> protocolResults = new LinkedHashMap<>();
    private double finalScore;
}
