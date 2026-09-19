package com.defecttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(
    basePackages = "com.defecttracker",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.defecttracker\\.modules\\..*"
    )
)
@EntityScan(basePackages = "com.defecttracker.entity")
@EnableJpaRepositories(basePackages = "com.defecttracker.repository")
@EnableAsync
@EnableScheduling
public class DefectTrackerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DefectTrackerApiApplication.class, args);
    }
}
