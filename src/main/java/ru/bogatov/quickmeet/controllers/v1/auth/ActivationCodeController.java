package ru.bogatov.quickmeet.controllers.v1.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.model.enums.VerificationSourceType;
import ru.bogatov.quickmeet.services.auth.VerificationService;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.AUTH + RouteConstants.CODE)
public class ActivationCodeController {

    private VerificationService verificationService;

    public ActivationCodeController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/send")
    public ResponseEntity sendActivationCode(@RequestParam String source, @RequestParam VerificationSourceType type) {
        return ResponseEntity.ok(verificationService.startVerification(source, type)); // создание записи PhoneNumberAсtivationRecord
    }

//    @PostMapping("/refresh")
//    public ResponseEntity refreshActivationCode(@RequestParam String phoneNumber) {
//        return ResponseEntity.ok(null); // обновление записи PhoneNumberAсtivationRecord
//    }

    @PostMapping("/confirm")
    public ResponseEntity confirmNumber(@RequestParam String source, @RequestParam String code) {
        return ResponseEntity.ok(verificationService.confirmVerification(code, source)); // сверка кода и проставление статуса true
    }

}
