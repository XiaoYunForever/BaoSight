package com.baoSight.service.evaluation.OPCUA;

import com.baoSight.DTO.model.evaluation.CaseEvaluationResult;

import com.baoSight.DTO.model.execution.CaseExecutionResult;
import com.baoSight.service.evaluation.Evaluation;
import org.springframework.stereotype.Service;

@Service("evalu_opcua_01")
public class opcua_01 implements Evaluation {

    @Override
    public CaseEvaluationResult evaluate(CaseExecutionResult caseExecutionResult) {
        final int maxScore = 20;
        int score = maxScore;

        CaseEvaluationResult caseEvaluationResult = new CaseEvaluationResult();
        caseEvaluationResult.setProtocolType("opcua");
        caseEvaluationResult.setCaseCode(caseExecutionResult.getOpcUaCaseData().getCaseCode());
        // 每次用例评分单独累积说明，最后统一写入 errorText。
        StringBuilder errors = new StringBuilder();
        String caseCode = caseEvaluationResult.getCaseCode();

        // 检查项 1：连接成功且读取到服务器时间。两种失败情况只扣一次分。
        if(!caseExecutionResult.getOpcUaCaseData().isConnected() || (caseExecutionResult.getOpcUaCaseData().getServerTimeText() == null)){
            int deductedScore = 20;
            score -= deductedScore;
            String reason = caseExecutionResult.getOpcUaCaseData().getErrorText();
            errors.append("编号 ").append(caseCode)
                    .append("：").append(reason)
                    .append("；扣 ").append(deductedScore).append(" 分\n");
        }
        // 后续独立检查项模板（启用时同时调整 maxScore，分配各项分值）：
        // if (检查项未通过) {
        //     int deductedScore = 5;
        //     score -= deductedScore;
        //     errors.append("编号 ").append(caseCode)
        //             .append("：检查项未通过的原因")
        //             .append("；扣 ").append(deductedScore).append(" 分\n");
        // }
        // 有前置依赖的检查项应先判断前置条件，避免同一原因重复扣分。
        //设置当前分数
        caseEvaluationResult.setScore(score);
        caseEvaluationResult.setMaxScore(maxScore);
        // 无扣分时为 null；有多项扣分时，每项占一行。
        caseEvaluationResult.setErrorText(errors.length() == 0 ? null : errors.toString().trim());

        return caseEvaluationResult;
    }
}
