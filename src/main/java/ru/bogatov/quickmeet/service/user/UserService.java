package ru.bogatov.quickmeet.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.entity.auth.UserForAuth;
import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.Role;
import ru.bogatov.quickmeet.model.request.LoginAfterVerificationForm;
import ru.bogatov.quickmeet.model.request.LoginForm;
import ru.bogatov.quickmeet.repository.userdata.UserRepository;
import ru.bogatov.quickmeet.model.request.RegistrationBody;
import ru.bogatov.quickmeet.service.auth.VerificationService;
import ru.bogatov.quickmeet.service.util.CityService;

import java.util.*;

import static ru.bogatov.quickmeet.constant.UserConstants.USER_LIST;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CityService cityService;
    private final VerificationService verificationService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CityService cityService, VerificationService verificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cityService = cityService;
        this.verificationService = verificationService;
    }

    public UserForAuth findUserForAuthById(UUID id) {
        return userRepository.findByPhoneNumberForAuth(id)
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND));
    }

    public User findUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND));
    }

    public User findUserByID(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND));
    }

    public List<User> findUsersByIdsList(Map<String, Set<UUID>> body) {
        return userRepository.findAllById(body.get(USER_LIST));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void updateRefreshToken(UUID id, String refreshToken) {
        userRepository.updateRefreshToken(id, refreshToken);
    }

    public User findActiveAndAvailableUserByPhoneAndPassword(LoginForm loginForm) {
        User user = userRepository.findByPhoneNumber(loginForm.getPhoneNumber())
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND));
        if (!passwordEncoder.matches(loginForm.getPassword(), user.getPassword())) {
            throw ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND);
        }
        checkUserStatus(user);
        return user;
    }

    public User findActiveAndAvailableUserByPhoneAndCheckVerification(String phoneNumber) {
        if (!verificationService.isVerified(phoneNumber)) {
            throw ErrorUtils.buildException(ApplicationError.AUTHENTICATION_ERROR, "Verification not found");
        }
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND));
        checkUserStatus(user);
        verificationService.deleteRecord(phoneNumber);
        return user;
    }
    @Transactional
    public User findUserAndResetPassword(LoginForm loginForm) {
        if (!verificationService.isVerified(loginForm.getPhoneNumber())) {
            throw ErrorUtils.buildException(ApplicationError.AUTHENTICATION_ERROR, "Verification not found");
        }
        User user = userRepository.findByPhoneNumber(loginForm.getPhoneNumber())
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND));
        checkUserStatus(user);
        user.setPassword(this.passwordEncoder.encode(loginForm.getPassword()));
        verificationService.deleteRecord(loginForm.getPhoneNumber());
        return userRepository.save(user);
    }

    private void checkUserStatus(User user) {
        if (user.isBlocked()) {
            throw ErrorUtils.buildException(ApplicationError.USER_IS_BLOCKED);
        }
        if (!user.isActive()) {
            throw ErrorUtils.buildException(ApplicationError.USER_IS_BLOCKED, "User not activated");
        }
        if (user.isRemoved()) {
            throw ErrorUtils.buildException(ApplicationError.USER_IS_BLOCKED, "User removed");
        }
    }

    public Set<UUID> findAllowedChatIds(UUID userID) {
        return userRepository.getAllApplicableChats(userID);
    }

    public User createUser(RegistrationBody body) {
        if (userRepository.isUserExistWithPhoneNumber(body.getPhoneNumber()).isPresent()) {
            throw ErrorUtils.buildException(ApplicationError.USER_EXISTS);
        }
        if (!verificationService.isVerified(body.getPhoneNumber())) {
            throw ErrorUtils.buildException(ApplicationError.BUSINESS_LOGIC_ERROR, "Phone not verified or verification expired");
        }
        verificationService.deleteRecord(body.getPhoneNumber());
        User user = new User();
        user.setCity(cityService.createOrGetExisting(body.getCityId(), body.getCityName()));
        user.setFirstName(body.getFirstName());
        user.setSecondName(body.getSecondName());
        user.setLastName(body.getLastName());
        user.setAccountClass(AccountClass.BASE);
        user.setAccountRank(5f);
        user.setMissSeries(0);
        user.setAttendSeries(0);
        user.setPhoneNumber(body.getPhoneNumber());
        user.setPassword(this.passwordEncoder.encode(body.getPassword()));
        user.setEmail(body.getEmail());
        user.setBirthDate(body.getBirthDate());
        user.setRegistrationDate(new Date());
        user.setRole(Role.USER);
        if (Boolean.TRUE.equals(body.getIsAdmin())) { //todo fix it
            user.setRole(Role.ADMIN);
        }
        user.setBlocked(false);
        user.setActive(true);
        user.setBlocked(false);
        return userRepository.save(user);
    }

    public boolean isUserExists(String phone) {
        return userRepository.findByPhoneNumber(phone).isPresent();
    }
}
