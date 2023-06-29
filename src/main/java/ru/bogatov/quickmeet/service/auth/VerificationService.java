package ru.bogatov.quickmeet.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entity.auth.VerificationRecord;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.VerificationSourceType;
import ru.bogatov.quickmeet.model.enums.VerificationStep;
import ru.bogatov.quickmeet.model.request.VerificationBody;
import ru.bogatov.quickmeet.repository.auth.PhoneVerificationRecordRepository;
import ru.bogatov.quickmeet.model.response.VerificationResponse;
import ru.bogatov.quickmeet.repository.userdata.UserRepository;

import java.util.Optional;
import java.util.Random;

import static ru.bogatov.quickmeet.constant.AuthConstants.*;

@Service
public class VerificationService {

    private final PhoneVerificationRecordRepository verificationRecordRepository;
    private final UserRepository userRepository;

    public VerificationService(PhoneVerificationRecordRepository phoneVerificationRecordRepository, UserRepository userRepository) {
        this.verificationRecordRepository = phoneVerificationRecordRepository;
        this.userRepository = userRepository;
    }

    @Value("${phone.confirmation.disable}")
    private boolean isTestCodeEnabled;

    public VerificationResponse startVerification(VerificationBody body) {
        try {
            if (body.getVerificationType().equals(VerificationSourceType.MAIL) && body.getVerificationStep().equals(VerificationStep.REGISTRATION)) {
                throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Type Mail and Step Registration not applicable");
            }
            switch (body.getVerificationStep()) {
                case REGISTRATION:
                    createOrUpdateExistingRecord(body.getSource(), body.getVerificationType(), true);
                case VERIFICATION:
                    createOrUpdateExistingRecord(body.getSource(), body.getVerificationType(), false);
            }
            //todo send code
            return VerificationResponse.builder().step(STEP_SEND_CODE).isSuccess(true).message(MESSAGE_CODE_SENT).build();
        } catch (RuntimeException ex) {
            return VerificationResponse.builder().step(STEP_SEND_CODE).isSuccess(false).message(ex.getMessage()).build();
        }
    }

    public boolean isVerified(String source) {
        try {
            VerificationRecord record = verificationRecordRepository.findBySource(source).orElseThrow(() -> ErrorUtils.buildException(ApplicationError.COMMON_ERROR));
            return record.getIsVerified() && VerificationSourceType.PHONE.equals(record.getType());
        } catch (RuntimeException e) {
            return false;
        }
    }

    public VerificationResponse confirmVerification(VerificationBody body) {
        VerificationRecord record = findBySource(body.getSource());
        if (Boolean.TRUE.equals(record.getIsVerified())) {
            return VerificationResponse.builder().step(STEP_CODE_VERIFY).isSuccess(true).message(MESSAGE_CODE_ALREADY_VERIFIED).build();
        }
        if (record.getActivationCode().equals(body.getCode())) {
            record.setIsVerified(true);
            verificationRecordRepository.save(record);
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

    private void createOrUpdateExistingRecord(String source, VerificationSourceType type, boolean checkExistingCustomer) {
        if (checkExistingCustomer && userRepository.isUserExistWithPhoneNumber(source).isPresent()) {
            throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Phone number already registered");
        }
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
        verificationRecordRepository.save(record);
    }


    private String generateCode() {
        if (this.isTestCodeEnabled) {
            return "0000";
        }
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

}
