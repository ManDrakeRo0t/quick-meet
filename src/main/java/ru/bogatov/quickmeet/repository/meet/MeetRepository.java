package ru.bogatov.quickmeet.repository.meet;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.bogatov.quickmeet.entity.Meet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MeetRepository extends JpaRepository<Meet, UUID> {
    @Timed("SQL-search-meet")
    @Query(nativeQuery = true, value = "select cast(id as varchar) as id from meet where meet_status in ?1 and category_id in ?2 and latitude between ?3 and ?4 and longevity between ?5 and ?6 and date_time between ?7 and ?8")
    Set<UUID> searchMeet(List<String> statuses, Set<UUID> categories, double lat_min, double lat_max, double lon_min, double lon_max, LocalDateTime from, LocalDateTime to);

    @Query(nativeQuery = true, value = "select cast(meet_id as varchar) as id from guest where guest.user_id = ?1")
    Set<UUID> findMeetsIdWhereUserGuest(UUID userId);

    @Query(nativeQuery = true, value = "select cast(id as varchar) as id from meet where user_id = ?1")
    Set<UUID> findMeetsIdWhereUserOwner(UUID userId);

    @Query(nativeQuery = true, value = "select cast(id as varchar) as id from meet where meet_status = 'PLANNED' and date_time < :now")
    Set<UUID> getStatusPlannedToActive(LocalDateTime now);
    @Query(nativeQuery = true, value = "select cast(id as varchar) from meet where meet_status = 'ACTIVE' and date_time + interval '1 hour' * expected_duration < :now limit :limit")
    Set<UUID> findMeedIdsShouldBeFinished(int limit ,LocalDateTime now);
    @Modifying
    @Query(nativeQuery = true, value = "update meet set meet_status = 'ACTIVE' where id = ?1")
    void setStatusActive(UUID meetId);


}
