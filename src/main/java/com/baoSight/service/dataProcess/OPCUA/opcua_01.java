//package com.baoSight.service.taskDispatcher.dataProcess.OPCUA;
//
//import com.baoSight.DTO.model.analysis.CaseAnalysisResult;
//import com.baoSight.DTO.model.execution.CaseExecutionResult;
//import com.baoSight.DTO.model.execution.opcua.OpcUaCaseData;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
//@Service("data_opcua_01")
//public class opcua_01 implements Dataprocess {
//    @Override
//    public CaseAnalysisResult process(CaseExecutionResult rawData) {
//        Objects.requireNonNull(rawData, "用例执行结果不能为空");
//        OpcUaCaseData opcUaCaseData = Objects.requireNonNull(
//                rawData.getOpcUaCaseData(), "OPC UA 用例数据不能为空");
//
//        // 本次连接是否成功；连接失败或未读取时，服务器时间可以为 null。
//        boolean connected = opcUaCaseData.isConnected();
//        String serverTimeText = opcUaCaseData.getServerTimeText();
//
//        Map<String,String> map = new HashMap<>();
//       // map.put("connected",connected);
//        map.put("serverTimeText",serverTimeText);
//        CaseAnalysisResult  caseAnalysisResult = new CaseAnalysisResult();
//        caseAnalysisResult.setResult(map);
//
//
//        return caseAnalysisResult;
//    }
//}
