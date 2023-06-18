package ru.bogatov.quickmeet.services.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entities.auth.VerificationRecord;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.VerificationSourceType;
import ru.bogatov.quickmeet.repositories.auth.PhoneVerificationRecordRepository;
import ru.bogatov.quickmeet.model.response.VerificationResponse;

import java.util.Optional;
import java.util.Random;

import static ru.bogatov.quickmeet.constants.AuthConstants.*;

@Service
public class VerificationService {

    private PhoneVerificationRecordRepository verificationRecordRepository;

    public VerificationService(PhoneVerificationRecordRepository phoneVerificationRecordRepository) {
        this.verificationRecordRepository = phoneVerificationRecordRepository;
    }

    @Value("${phone.confirmation.disable}")
    private boolean isTestCodeEnabled;

    public VerificationResponse startVerification(String source, VerificationSourceType type) {
        try {
            createOrUpdateExistingRecord(source, type);
            if (!isTestCodeEnabled) {
                //send code by sms
            } //todo constants
            return VerificationResponse.builder().step(STEP_SEND_CODE).isSuccess(false).message(MESSAGE_CODE_SENT).build();
        } catch (RuntimeException ex) {
            return VerificationResponse.builder().step(STEP_SEND_CODE).isSuccess(true).message(ex.getMessage()).build();
        }

    }

    public boolean isVerified(String source) {
        try {
            return verificationRecordRepository.findBySource(source).get().getIsVerified();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public VerificationResponse confirmVerification(String code, String phoneNumber) {
        VerificationRecord record = findBySource(phoneNumber);
        record.setIsVerified(true);
        verificationRecordRepository.save(record);
        if (code.equals(record.getActivationCode())) {
            return VerificationResponse.builder().step(STEP_CODE_VERIFY).isSuccess(true).message(MESSAGE_CODE_VERIFIED).build();
        }
        return VerificationResponse.builder().step(STEP_CODE_VERIFY).isSuccess(false).message(MESSAGE_CODE_NOT_VERIFIED).build();
    }

    public void deleteRecord(String phoneNumber) {
        VerificationRecord existingRecord = findBySource(phoneNumber);
        verificationRecordRepository.delete(existingRecord);
    }

    private VerificationRecord findBySource(String source) {
        Optional<VerificationRecord> existingRecord = verificationRecordRepository.findBySource(source);
        if (existingRecord.isPresent()) {
            return existingRecord.get();
        } else {
            throw ErrorUtils.buildException(ApplicationError.COMMON_ERROR, MESSAGE_RECORD_NOT_FOUND);
        }
    }

    private VerificationRecord createOrUpdateExistingRecord(String source, VerificationSourceType type) {
        Optional<VerificationRecord> existingRecord = verificationRecordRepository.findBySource(source);
        VerificationRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
            if (Boolean.TRUE.equals(record.getIsVerified())) {
                throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Record already verified");
            }
        } else {
            record = new VerificationRecord();
            record.setIsVerified(false);
            record.setType(type);
            record.setSource(source);
        }
        record.setActivationCode(generateCode());
        return verificationRecordRepository.save(record);
    }


    private String generateCode() {
        if (this.isTestCodeEnabled) {
            return "0000";
        }
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

}
