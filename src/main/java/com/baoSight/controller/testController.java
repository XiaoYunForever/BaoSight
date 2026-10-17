package com.baoSight.controller;



import com.baoSight.DTO.api.request.CreateTestRequest;
import com.baoSight.service.taskDispatcher.mainRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/index")
public class testController {
    @Autowired
    mainRun mainrun;

    @PostMapping("/test")
    public void test(@RequestBody CreateTestRequest createTestRequest) throws Exception {
        System.out.println(createTestRequest.toString());
        mainrun.run(createTestRequest);
    }

    @GetMapping("/cases")
    public List<Map<String, Object>> getCases() {
        // 先用示例数据验证前后端是否连通
        return List.of(
                Map.<String, Object>of(
                        "id", "601",
                        "name", "服务器连接测试",
                        "category", "连接管理",
                        "description", "验证 OPC UA 服务器连接与响应",
                        "seconds", 300
                )
        );
    }

}
