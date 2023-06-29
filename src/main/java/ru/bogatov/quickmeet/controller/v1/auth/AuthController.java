package ru.bogatov.quickmeet.controller.v1.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.model.request.LoginForm;
import ru.bogatov.quickmeet.model.request.RegistrationBody;
import ru.bogatov.quickmeet.model.response.AuthenticationResponse;
import ru.bogatov.quickmeet.service.auth.AuthenticationService;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.AUTH)
public class AuthController {

    private final AuthenticationService authenticationService;
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Validated LoginForm userFromLogin){
        return ResponseEntity.ok(authenticationService.login(userFromLogin));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerNewUser(@RequestBody @Validated RegistrationBody body){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.register(body));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity resetPassword(@RequestParam String email) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/updatePassword")
    public ResponseEntity resetPasswordByCode(@RequestBody LoginForm body) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/refresh/{refreshToken}")
    public ResponseEntity<AuthenticationResponse> refreshToken(@PathVariable String refreshToken){
        return ResponseEntity.ok(authenticationService.refreshTokenPair(refreshToken));
    }

    @GetMapping("/activation")
    public ResponseEntity activateAccount(@RequestParam("code") String code) {
        return ResponseEntity.ok(null);
    }
}
