package ru.bogatov.quickmeet.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.bogatov.quickmeet.entity.auth.VerificationRecord;

import java.util.Optional;
import java.util.UUID;


public interface PhoneVerificationRecordRepository extends JpaRepository<VerificationRecord, UUID> {

    Optional<VerificationRecord> findBySource(String source);

    @Query(nativeQuery = true, value = "select cast(id as varchar) as id from usr where phone_number = ?1")
    Optional<UUID> isUserExists(String phone);

}
