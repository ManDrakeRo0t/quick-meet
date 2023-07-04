package ru.bogatov.quickmeet.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bogatov.quickmeet.entity.auth.VerificationRecord;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.VerificationSourceType;
import ru.bogatov.quickmeet.model.enums.VerificationStep;
import ru.bogatov.quickmeet.model.request.VerificationBody;
import ru.bogatov.quickmeet.repository.auth.PhoneVerificationRecordRepository;
import ru.bogatov.quickmeet.model.response.VerificationResponse;
import ru.bogatov.quickmeet.repository.userdata.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import static ru.bogatov.quickmeet.constant.AuthConstants.*;

@Service
@Slf4j
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
            String code;
            switch (body.getVerificationStep()) {
                case REGISTRATION:
                    code = createOrUpdateExistingRecordForRegistration(body.getSource(), body.getVerificationType(), true);
                    break;
                case VERIFICATION:
                    code = createOrUpdateExistingRecordForVerification(body.getSource(), body.getVerificationType(), true);
                    break;
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
            if (record.getActualTo() == null || record.getActualTo().isBefore(LocalDateTime.now())) {
                log.debug("record with source {} not actual", record.getSource());
                return false;
            }
            return record.getIsVerified() && VerificationSourceType.PHONE.equals(record.getType());
        } catch (RuntimeException e) {
            return false;
        }
    }
    @Transactional
    public VerificationResponse confirmVerification(VerificationBody body) {
        VerificationRecord record = findBySource(body.getSource());
        if (Boolean.TRUE.equals(record.getIsVerified())) {
            return VerificationResponse.builder().step(STEP_CODE_VERIFY).isSuccess(true).message(MESSAGE_CODE_ALREADY_VERIFIED).build();
        }
        if (record.getActivationCode().equals(body.getCode())) {
            switch (body.getVerificationType()) {
                case PHONE:
                    record.setIsVerified(true);
                    record.setActualTo(LocalDateTime.now().plusMinutes(2)); //todo move magic number to properties
                    verificationRecordRepository.save(record);
                    break;
                case MAIL:
                    userRepository.setMailConfirmation(body.getSource(), true);
                    deleteRecord(body.getSource());
            }
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

    private String createOrUpdateExistingRecordForRegistration(String source, VerificationSourceType type, boolean checkExistingCustomer) {
        if (checkExistingCustomer && userRepository.isUserExistWithPhoneNumber(source).isPresent()) {
            throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Phone number already registered");
        }
        return createOrUpdateRecord(source, type);
    }

    private String createOrUpdateExistingRecordForVerification(String source, VerificationSourceType type, boolean checkExistingCustomer) {
        switch (type) {
            case MAIL:
                if (checkExistingCustomer && userRepository.isUserExistWithMail(source).isEmpty()) {
                    throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "User with mail not found");
                }
                break;
            case PHONE:
                if (checkExistingCustomer && userRepository.isUserExistWithPhoneNumber(source).isEmpty()) {
                    throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "User with phone not found");
                }
        }
        return createOrUpdateRecord(source, type);
    }

    private String createOrUpdateRecord(String source, VerificationSourceType type) {
        Optional<VerificationRecord> existingRecord = verificationRecordRepository.findBySource(source);
        VerificationRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
            if (Boolean.TRUE.equals(record.getIsVerified()) && record.getActualTo() != null && LocalDateTime.now().isBefore(record.getActualTo())) {
                throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Record already verified");
            }
        } else {
            record = new VerificationRecord();
            record.setIsVerified(false);
            record.setType(type);
            record.setSource(source);
        }
        String code = generateCode();
        record.setActivationCode(code);
        if (record.getActualTo() != null) {
            record.setActualTo(null);
            record.setIsVerified(false);
        }
        verificationRecordRepository.save(record);
        return code;
    }


    private String generateCode() {
        if (this.isTestCodeEnabled) {
            return "0000";
        }
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

}
