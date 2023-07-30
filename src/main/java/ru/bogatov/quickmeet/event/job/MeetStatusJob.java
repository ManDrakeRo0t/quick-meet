package ru.bogatov.quickmeet.event.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.bogatov.quickmeet.service.meet.MeetService;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MeetStatusJob {

    @Value("${application.meet-status-job}")
    boolean isJobEnabled;

    @Value(value = "${application.meet-job-limit}")
    private int meetJobLimit;
    private final MeetService meetService;

    public MeetStatusJob(MeetService meetService) {
        this.meetService = meetService;
    }
    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void updateMeetStatusPlannedToActive() {
        if (isJobEnabled) {
            log.info("Update Meet Status job [PLANNED -> ACTIVE] - started");
            meetService.changeStatusPlannedToActive();
            log.info("Update Meet Status job [PLANNED -> ACTIVE] - finished");
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void updateMeetStatusActiveToFinished() {
        if (isJobEnabled) {
            log.info("Update Meet Status job [ACTIVE -> FINISHED] - started");
            meetService.changeStatusActiveToFinished(meetJobLimit);
            log.info("Update Meet Status job [ACTIVE -> FINISHED] - finished");
        }
    }
}
