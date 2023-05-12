package ru.bogatov.quickmeet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
public class QuickMeetApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuickMeetApplication.class, args);
    }
}
