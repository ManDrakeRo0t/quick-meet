package ru.bogatov.quickmeet.controllers.v1.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.request.LoginForm;
import ru.bogatov.quickmeet.request.RegistrationBody;
import ru.bogatov.quickmeet.response.AuthenticationResponse;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.AUTH)
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginForm userFromLogin){
        return ResponseEntity.ok(new AuthenticationResponse());
    }

    @PostMapping("/register")
    public ResponseEntity registerNewUser(@RequestBody @Validated RegistrationBody user){
        return ResponseEntity.ok(new AuthenticationResponse()); //удаление записи PhoneNumberAсtivationRecord
    }

    @PostMapping("/resetPassword")
    public ResponseEntity resetPassword(@RequestParam String email) {
        return ResponseEntity.ok(new AuthenticationResponse());
    }

    @PostMapping("/updatePassword")
    public ResponseEntity resetPasswordByCode(@RequestBody LoginForm body) {
        return ResponseEntity.ok(new AuthenticationResponse());
    }

    @PostMapping("/refresh/{refreshToken}")
    public ResponseEntity<String> refreshToken(@PathVariable String refreshToken){
        return ResponseEntity.ok("response");
    }

    @GetMapping("/activation")
    public ResponseEntity activateAccount(@RequestParam("code") String code) {
        return ResponseEntity.ok(new AuthenticationResponse());
    }
}
