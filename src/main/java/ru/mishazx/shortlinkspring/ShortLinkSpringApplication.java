package ru.mishazx.shortlinkspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("ru.mishazx.shortlinkspring.model")
@EnableJpaRepositories("ru.mishazx.shortlinkspring.repository")
@EnableScheduling
public class ShortLinkSpringApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ShortLinkSpringApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
