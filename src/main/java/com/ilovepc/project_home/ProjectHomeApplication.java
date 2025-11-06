package com.ilovepc.project_home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ProjectHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectHomeApplication.class, args);
    }

}
