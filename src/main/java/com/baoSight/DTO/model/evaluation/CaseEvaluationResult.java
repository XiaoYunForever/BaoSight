package com.baoSight.DTO.model.evaluation;

import lombok.Data;

/** 单个用例的评分结果，通过 protocolType 区分协议。 */
@Data
public class CaseEvaluationResult {
    private String protocolType;
    private String caseCode;
    private int score;
    private int maxScore;
    private String errorText;
}