package ru.bogatov.quickmeet.controllers.v1.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.services.auth.PhoneVerificationService;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.AUTH + RouteConstants.CODE)
public class PhoneActivationCodeController {

    private PhoneVerificationService phoneVerificationService;

    public PhoneActivationCodeController(PhoneVerificationService phoneVerificationService) {
        this.phoneVerificationService = phoneVerificationService;
    }

    @PostMapping("/send")
    public ResponseEntity sendActivationCode(@RequestParam String phoneNumber) {
        return ResponseEntity.ok(phoneVerificationService.startVerification(phoneNumber)); // создание записи PhoneNumberAсtivationRecord
    }

//    @PostMapping("/refresh")
//    public ResponseEntity refreshActivationCode(@RequestParam String phoneNumber) {
//        return ResponseEntity.ok(null); // обновление записи PhoneNumberAсtivationRecord
//    }

    @PostMapping("/confirm")
    public ResponseEntity confirmNumber(@RequestParam String phoneNumber, @RequestParam String code) {
        return ResponseEntity.ok(phoneVerificationService.confirmVerification(code, phoneNumber)); // сверка кода и проставление статуса true
    }

}
