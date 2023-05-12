package ru.bogatov.quickmeet.controllers.v1.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import ru.bogatov.quickmeet.constants.RouteConstants;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.AUTH + RouteConstants.CODE)
public class PhoneActivationCodeController {

    @PostMapping("/send")
    public ResponseEntity sendActivationCode(@RequestPart String phoneNumber) {
        return ResponseEntity.ok(null); // создание записи PhoneNumberAсtivationRecord
    }

    @PostMapping("/refresh")
    public ResponseEntity refreshActivationCode(@RequestPart String phoneNumber) {
        return ResponseEntity.ok(null); // оьновление записи PhoneNumberAсtivationRecord
    }

    @PostMapping("/confirm")
    public ResponseEntity confirmNumber(@RequestPart String phoneNumber, @RequestPart String code) {
        return ResponseEntity.ok(null); // сверка кода и проставление статуса true
    }

}
