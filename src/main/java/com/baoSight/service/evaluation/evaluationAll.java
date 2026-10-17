package com.baoSight.service.evaluation;

import com.baoSight.DTO.model.evaluation.*;
import java.util.*;

public class evaluationAll {
    private static final Map<String, Double> WEIGHTS = Map.of("opcua", 60.0, "ethernetip", 40.0);

    public float evaluate(ArrayList<CaseEvaluationResult> results) {
        return (float) summarize(results).getFinalScore();
    }

    public TestEvaluationResult summarize(List<CaseEvaluationResult> results) {
        return summarize(results, WEIGHTS);
    }

    /** 先按协议计算百分制得分，再按本次实际运行协议的权重归一化。 */
    public TestEvaluationResult summarize(List<CaseEvaluationResult> results, Map<String, Double> weights) {
        Objects.requireNonNull(results, "评分结果列表不能为空");
        Objects.requireNonNull(weights, "协议权重不能为空");

        TestEvaluationResult report = new TestEvaluationResult();

        // 对单个测试协议做累加，计算maxscore 和 score
        for (CaseEvaluationResult result : results) {

            Objects.requireNonNull(result, "用例评分结果不能为空");
            String protocol = Objects.requireNonNull(result.getProtocolType(), "协议类型不能为空");

            if (result.getMaxScore() <= 0 || result.getScore() < 0 || result.getScore() > result.getMaxScore()) {
                throw new IllegalArgumentException("用例分数不合法：" + result.getCaseCode());
            }
            //判断是否有误

            // 新建一个汇总类，summary
            ProtocolEvaluationSummary summary = report.getProtocolResults().computeIfAbsent(protocol, key -> {
                ProtocolEvaluationSummary value = new ProtocolEvaluationSummary();
                value.setProtocolType(key);
                return value;
            });

            summary.getCaseResults().add(result);
            summary.setScore(summary.getScore() + result.getScore());
            summary.setMaxScore(summary.getMaxScore() + result.getMaxScore());
        }
        if (results.isEmpty()) {
            return report;
        }
        double activeWeight = 0;
        for (String protocol : report.getProtocolResults().keySet()) {
            Double weight = weights.get(protocol);
            if (weight == null || !Double.isFinite(weight) || weight <= 0) {
                throw new IllegalArgumentException("实际测试协议必须配置正数权重：" + protocol);
            }
            activeWeight += weight;
        }
        if (!Double.isFinite(activeWeight)) {
            throw new IllegalArgumentException("协议权重总和超出范围");
        }
        double finalScore = 0;
        for (ProtocolEvaluationSummary summary : report.getProtocolResults().values()) {
            summary.setPercentage(100.0 * summary.getScore() / summary.getMaxScore());
            summary.setWeight(weights.get(summary.getProtocolType()) / activeWeight * 100.0);
            summary.setWeightedScore(summary.getPercentage() * summary.getWeight() / 100.0);
            finalScore += summary.getWeightedScore();
        }
        report.setFinalScore(finalScore);
        return report;
    }
}
