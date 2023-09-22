package ru.bogatov.quickmeetmessenger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.bogatov.quickmeetmessenger.entity.MeetGuestRecord;

import java.util.Set;
import java.util.UUID;

public interface MeetGuestRecordRepository extends JpaRepository<MeetGuestRecord, UUID> {
    @Query(nativeQuery = true, value = "select cast(user_id as varchar) as user_id from guest_record where meet_id = :meetId")
    Set<UUID> findUsersByMeetId(UUID meetId);

    void deleteMeetGuestRecordByMeetIdAndUserId(UUID meetId, UUID userId);
}
