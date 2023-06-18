package ru.bogatov.quickmeet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
public class QuickMeetApplication {
    /* todo Add Account class to JWT token and sessions data [available chats]
    *  rework verification record logic
    *  add TYPE REGISTER OR CONFIRM to confirmCode and check that phone is free
    * */

    public static void main(String[] args) {
        SpringApplication.run(QuickMeetApplication.class, args);
    }
}
