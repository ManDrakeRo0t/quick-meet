package ru.bogatov.quickmeet.repositories.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.bogatov.quickmeet.entities.auth.PhoneNumberVerificationRecord;

import java.util.Optional;
import java.util.UUID;


public interface PhoneVerificationRecordRepository extends JpaRepository<PhoneNumberVerificationRecord, UUID> {

    Optional<PhoneNumberVerificationRecord> findByPhoneNumber(String phoneNumber);

}
