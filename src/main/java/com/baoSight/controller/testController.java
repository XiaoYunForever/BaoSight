package com.baoSight.controller;



import com.baoSight.DTO.api.request.CreateTestRequest;
import com.baoSight.DTO.model.evaluation.TestEvaluationResult;
import com.baoSight.service.taskDispatcher.mainRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/index")
public class testController {
    @Autowired
    mainRun mainrun;

    @PostMapping(value = "/test", consumes = "application/json", produces = "application/json")
    public TestEvaluationResult test(@RequestBody CreateTestRequest createTestRequest) throws Exception {
        if (createTestRequest.getCodelist() == null || createTestRequest.getCodelist().isEmpty()
                || createTestRequest.getMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "codelist 和 mode 不能为空");
        }
        return mainrun.run(createTestRequest);
    }


}
