package ru.bogatov.quickmeet.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.service.user.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static ru.bogatov.quickmeet.constant.UserConstants.MIN_AUTO_UPDATE_RANK;
import static ru.bogatov.quickmeet.constant.UserConstants.RANK_UPDATE_DELTA;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@Slf4j
public class UserServiceTest {


    @Test
    public void userWithRankMore2WillNotUpdated() {
        User user = new User();
        user.setAccountRank(5.0f);
        user.setLastRankUpdateDate(null);
        user.setId(UUID.randomUUID());

        processUserRating(user);

        assertNull(user.getLastRankUpdateDate());

    }

    @Test
    public void userWithRankLess2WillUpdated() {
        User user = new User();
        user.setAccountRank(1.9f);
        user.setLastRankUpdateDate(null);
        user.setId(UUID.randomUUID());


        processUserRating(user);

        assertTrue(user.getAccountRank() == 2.0f);
        assertTrue(user.getLastRankUpdateDate() != null);
        log.info(String.valueOf(user.getLastRankUpdateDate()));
    }

    @Test
    public void userWithRankLess2WillUpdatedOnce() {
        float rankBefore = 1.7f;
        User user = new User();
        user.setAccountRank(rankBefore);
        user.setLastRankUpdateDate(null);
        user.setId(UUID.randomUUID());


        processUserRating(user);

        assertTrue(user.getAccountRank() == rankBefore + RANK_UPDATE_DELTA);
        assertTrue(user.getLastRankUpdateDate() != null);
        log.info(String.valueOf(user.getLastRankUpdateDate()));

        processUserRating(user);

        assertTrue(user.getAccountRank() == rankBefore + RANK_UPDATE_DELTA);
        assertTrue(user.getLastRankUpdateDate() != null);
        log.info(String.valueOf(user.getLastRankUpdateDate()));
    }

    @Test
    public void userWithRankLess2WillUpdatedNotMax() {
        float rankBefore = 1.1f;
        Calendar calendar = Calendar.getInstance();
        calendar.set(2023,8,8);
        User user = new User();
        user.setAccountRank(rankBefore);
        user.setLastRankUpdateDate(new Date(calendar.getTimeInMillis()));
        user.setId(UUID.randomUUID());


        processUserRating(user);

        assertTrue(user.getAccountRank() > 1.3f);
        assertTrue(user.getLastRankUpdateDate() != null);
        log.info(String.valueOf(user.getLastRankUpdateDate()));

    }


    public User processUserRating(User user) {
        Date lastUpdate = user.getLastRankUpdateDate();
        float currentRank = user.getAccountRank();
        Date today = new Date();
        if (currentRank < MIN_AUTO_UPDATE_RANK) {
            if (lastUpdate == null) {
                user.setLastRankUpdateDate(today);
                user.setAccountRank(currentRank + RANK_UPDATE_DELTA);
            } else {
                long diff = ChronoUnit.DAYS.between(Instant.ofEpochMilli(lastUpdate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now());
                if (diff >= 1) {
                    currentRank += RANK_UPDATE_DELTA * diff;
                    if (currentRank > MIN_AUTO_UPDATE_RANK) {
                        currentRank = MIN_AUTO_UPDATE_RANK;
                        lastUpdate = null;
                    } else {
                        lastUpdate = today;
                    }
                    user.setLastRankUpdateDate(lastUpdate);
                    user.setAccountRank(currentRank);
                }
            }
        }
        return user;
    }

}
