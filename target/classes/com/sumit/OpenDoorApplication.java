package com.sumit;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@Slf4j
@SpringBootApplication
@EnableRabbit
@EnableScheduling
@IntegrationComponentScan
@EnableAsync
@EnableFeignClients
@ServletComponentScan
@EnableAutoConfiguration
public class OpenDoorApplication {
    static {
        System.setProperty("jdk.tls.maxHandshakeMessageSize", "50000");
    }

    public static void main(String[] args) {
        SpringApplication.run(OpenDoorApplication.class, args);
        log.info("*** OpenDoor  Application Started ***");
    }
}