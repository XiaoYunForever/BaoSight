package com.baoSight.controller;



import com.baoSight.DTO.api.request.CreateTestRequest;
import com.baoSight.service.taskDispatcher.mainRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/index")
public class testController {
    @Autowired
    mainRun mainrun;

    @PostMapping("/test")
    public void test(@RequestBody CreateTestRequest createTestRequest) throws Exception {
        mainrun.run(createTestRequest);
    }


}
