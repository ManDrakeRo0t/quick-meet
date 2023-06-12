package ru.bogatov.quickmeet.services.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entities.auth.PhoneNumberVerificationRecord;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.repositories.auth.PhoneVerificationRecordRepository;
import ru.bogatov.quickmeet.model.response.VerificationResponse;

import java.util.Optional;
import java.util.Random;

import static ru.bogatov.quickmeet.constants.AuthConstants.*;

@Service
public class PhoneVerificationService {

    private PhoneVerificationRecordRepository phoneVerificationRecordRepository;

    public PhoneVerificationService(PhoneVerificationRecordRepository phoneVerificationRecordRepository) {
        this.phoneVerificationRecordRepository = phoneVerificationRecordRepository;
    }

    @Value("${phone.confirmation.disable}")
    private boolean isTestCodeEnabled;

    public VerificationResponse startVerification(String phoneNumber) {
        PhoneNumberVerificationRecord record = createOrUpdateExistingRecord(phoneNumber);
        if (!isTestCodeEnabled) {
            //send code by sms
        } //todo constants
        return VerificationResponse.builder().step(STEP_SEND_CODE).isSuccess(true).message(MESSAGE_CODE_SENT).build();
    }

    public VerificationResponse confirmVerification(String code, String phoneNumber) {
        PhoneNumberVerificationRecord record = findByPhoneNumber(phoneNumber);
        if (code.equals(record.getActivationCode())) {
            return VerificationResponse.builder().step(STEP_CODE_VERIFY).isSuccess(true).message(MESSAGE_CODE_VERIFIED).build();
        }
        return VerificationResponse.builder().step(STEP_CODE_VERIFY).isSuccess(false).message(MESSAGE_CODE_NOT_VERIFIED).build();
    }

    public void deleteRecord(String phoneNumber) {
        PhoneNumberVerificationRecord existingRecord = findByPhoneNumber(phoneNumber);
        phoneVerificationRecordRepository.delete(existingRecord);
    }

    private PhoneNumberVerificationRecord findByPhoneNumber(String phoneNumber) {
        Optional<PhoneNumberVerificationRecord> existingRecord = phoneVerificationRecordRepository.findByPhoneNumber(phoneNumber);
        if (existingRecord.isPresent()) {
            return existingRecord.get();
        } else {
            throw ErrorUtils.buildException(ApplicationError.COMMON_ERROR, MESSAGE_RECORD_NOT_FOUND);
        }
    }

    private PhoneNumberVerificationRecord createOrUpdateExistingRecord(String phoneNumber) {
        Optional<PhoneNumberVerificationRecord> existingRecord = phoneVerificationRecordRepository.findByPhoneNumber(phoneNumber);
        PhoneNumberVerificationRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
        } else {
            record = new PhoneNumberVerificationRecord();
            record.setPhoneNumber(phoneNumber);
        }
        record.setActivationCode(generateCode());
        return phoneVerificationRecordRepository.save(record);
    }

    private String generateCode() {
        if (this.isTestCodeEnabled) {
            return "0000";
        }
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

}
