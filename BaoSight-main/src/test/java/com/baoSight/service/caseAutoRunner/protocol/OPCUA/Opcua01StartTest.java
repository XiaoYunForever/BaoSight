package com.baoSight.service.caseAutoRunner.protocol.OPCUA;

import com.baoSight.service.caseAutoRunner.protocol.Protocol;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/** 集成测试：通过 Spring 获取 OPC UA 用例并执行完整 start() 流程。 */
class Opcua01StartTest {

    @Test
    void runOpcua01Start() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {
            Protocol testCase = context.getBean("case_opcua_01", Protocol.class);
            testCase.start();
        }
    }

    @Configuration
    @PropertySource("classpath:application.properties")
    static class TestConfig {
        @Bean
        static PropertySourcesPlaceholderConfigurer properties() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean("case_opcua_01")
        opcua_01 opcua01() {
            return new opcua_01();
        }
    }
}
