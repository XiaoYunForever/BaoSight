package com.baoSight.controller;


import com.baoSight.DTO.UserTestDTO;
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
    public UserTestDTO test(@RequestBody UserTestDTO usertestdto) {
        mainrun.run(usertestdto);

        return null;
    }


}
