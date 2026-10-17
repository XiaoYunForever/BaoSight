package com.baoSight.DTO.model.evaluation;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProtocolEvaluationSummary {
    private String protocolType;
    private long score;
    private long maxScore;
    private double percentage;
    private double weight;
    private double weightedScore;
    private List<CaseEvaluationResult> caseResults = new ArrayList<>();
}
