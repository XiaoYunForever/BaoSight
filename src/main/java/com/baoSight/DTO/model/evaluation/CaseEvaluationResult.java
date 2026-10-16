package com.baoSight.DTO.model.evaluation;

import com.baoSight.enums.Verdict;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CaseEvaluationResult {
    private String caseCode;
    private int score;
    private int maxScore;

}
