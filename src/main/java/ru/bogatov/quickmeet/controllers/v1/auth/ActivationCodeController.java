package ru.bogatov.quickmeet.controllers.v1.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.model.enums.VerificationSourceType;
import ru.bogatov.quickmeet.model.request.VerificationBody;
import ru.bogatov.quickmeet.services.auth.VerificationService;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.AUTH + RouteConstants.CODE)
public class ActivationCodeController {

    private VerificationService verificationService;

    public ActivationCodeController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/send")
    public ResponseEntity sendActivationCode(@RequestBody VerificationBody body) {
        return ResponseEntity.ok(verificationService.startVerification(body)); // создание записи PhoneNumberAсtivationRecord
    }

//    @PostMapping("/refresh")
//    public ResponseEntity refreshActivationCode(@RequestParam String phoneNumber) {
//        return ResponseEntity.ok(null); // обновление записи PhoneNumberAсtivationRecord
//    }

    @PostMapping("/confirm")
    public ResponseEntity confirmNumber(@RequestBody VerificationBody body) {
        return ResponseEntity.ok(verificationService.confirmVerification(body));
    }

}
