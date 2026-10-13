package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.DTO.api.request.CreateTestRequest;
import com.baoSight.DTO.model.common.DeviceInfo;
import com.baoSight.enums.ExecutionMode;
import com.baoSight.service.taskDispatcher.mainRun;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.ArrayList;
import java.util.Collections;

/** 集成测试：从 mainRun.run() 进入实际用例调度流程，会启动浏览器。 */
class Opcua01StartTest {

    @Test
    void runMainRun() throws Exception {
        CreateTestRequest request = new CreateTestRequest();
        request.setCodelist(new ArrayList<>(Collections.singletonList("601")));
        request.setMode(ExecutionMode.SIMULATION);
        request.setDevice(new DeviceInfo());

        // 注意：目前 mainRun.run() 内部还会覆盖上述请求参数。
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {
            mainRun runner = context.getBean(mainRun.class);
            runner.run(request);
        }
    }

    @Configuration
    @ComponentScan(basePackages = "com.baoSight.service")
    @PropertySource("classpath:application.properties")
    static class TestConfig {
        @Bean
        static PropertySourcesPlaceholderConfigurer properties() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }
}
