package com.tgfc.tw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = {"com.tgfc.tw.*"})
@EntityScan(basePackages = {"com.tgfc.tw.entity"})
@EnableJpaRepositories(basePackages = {"com.tgfc.tw.repository"})
@EnableScheduling
public class AmsExampleApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AmsExampleApplication.class, args);
    }

    @PostConstruct
    void started() {
        // set JVM timezone as UTC
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
    }
}
