package ru.bogatov.quickmeet.repository.meet;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.bogatov.quickmeet.entity.Meet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MeetRepository extends JpaRepository<Meet, UUID> {
    @Timed("SQL-search-meet")
    @Query(nativeQuery = true, value = "select * from meet where meet_status in ?1 and category_id in ?2 and latitude between ?3 and ?4 and longevity between ?5 and ?6 and date_time between ?7 and ?8")
    List<Meet> searchMeet(List<String> statuses, Set<UUID> categories, double lat_min, double lat_max, double lon_min, double lon_max, LocalDateTime from, LocalDateTime to);

    @Query(nativeQuery = true, value = "select * from meet where id in (select meet_id from guest where meet.user_id = ?1)")
    List<Meet> findMeetsIdWhereUserGuest(UUID userId);

    @Query(nativeQuery = true, value = "select * from meet where user_id = ?1")
    List<Meet> findMeetsIdWhereUserOwner(UUID userId);

}