package ru.bogatov.quickmeet.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.entity.auth.UserForAuth;
import ru.bogatov.quickmeet.model.auth.CustomUserDetails;
import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.Role;
import ru.bogatov.quickmeet.model.request.LoginForm;
import ru.bogatov.quickmeet.model.request.UserUpdateBody;
import ru.bogatov.quickmeet.repository.userdata.UserRepository;
import ru.bogatov.quickmeet.model.request.RegistrationBody;
import ru.bogatov.quickmeet.service.auth.VerificationService;
import ru.bogatov.quickmeet.service.file.FileService;

import java.util.*;

import static ru.bogatov.quickmeet.constant.CacheConstants.USERS_CACHE;
import static ru.bogatov.quickmeet.constant.UserConstants.USER_LIST;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final CacheManager cacheManager;
    private final FileService fileService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, VerificationService verificationService, CacheManager cacheManager, FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.cacheManager = cacheManager;
        this.fileService = fileService;
    }
    // no need after caches
    public UserForAuth findUserForAuthById(UUID id) {
        return userRepository.findByPhoneNumberForAuth(id)
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND));
    }

    public User findUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND));
    }

    public User updateUser(UUID id, UserUpdateBody body) {
        User user = findUserByID(id);
        checkUserStatus(user);
        if (body.getEmail() != null && !body.getEmail().isEmpty()) {
            user.setEmailConfirmed(false);
            user.setEmail(body.getEmail());
        }
        if (body.getFirstName() != null && !body.getFirstName().isEmpty()) {
            user.setFirstName(body.getFirstName());
        }
        if (body.getSecondName() != null && !body.getSecondName().isEmpty()) {
            user.setSecondName(body.getSecondName());
        }
        if (body.getDescription() != null && !body.getDescription().isEmpty()) {
            user.setDescription(body.getDescription());
        }
        if (body.isDeleted()) {
            user.setRemoved(true);
        }
        if (body.getRole() != null) {
            if (!((CustomUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal()).getRoleSet().contains(Role.ADMIN)) {
                throw ErrorUtils.buildException(ApplicationError.AUTHENTICATION_ERROR, "Only Admin can change role");
            } else {
                user.setRole(body.getRole());
            }
        }
        return updateUser(user);
    }
    @Cacheable(value = USERS_CACHE, key = "#id")
    public User findUserByID(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.USER_NOT_FOUND));
    }

    public List<User> findUsersByIdsList(Map<String, Set<UUID>> body) {
        List<User> result = new ArrayList<>();
        Set<UUID> requestList = body.get(USER_LIST);
        Set<UUID> notFoundInCache = new HashSet<>();
        //todo check for NPE where cache empty
        requestList.forEach(uuid -> {
            User user = cacheManager.getCache(USERS_CACHE).get(uuid, User.class);
            if (user != null) {
                result.add(user);
            } else {
                notFoundInCache.add(uuid);
            }
        });
        List<User> userFormDb = userRepository.findAllById(notFoundInCache);
        userFormDb.forEach(user -> cacheManager.getCache(USERS_CACHE).put(user.getId(), user));
        result.addAll(userFormDb);
        return result;
    }
    public User updateUser(User user) {
        User updatedUser = userRepository.save(user);
        cacheManager.getCache(USERS_CACHE).put(updatedUser.getId(), updatedUser);
        return updatedUser;
    }

    public void updateRefreshToken(UUID id, String refreshToken) {
        userRepository.updateRefreshToken(id, refreshToken);
        cacheManager.getCache(USERS_CACHE).evict(id);
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
        if (this.passwordEncoder.matches(loginForm.getPassword(), user.getPassword())) {
            throw ErrorUtils.buildException(ApplicationError.AUTHENTICATION_ERROR, "Old password match new one");
        }
        user.setPassword(this.passwordEncoder.encode(loginForm.getPassword()));
        verificationService.deleteRecord(loginForm.getPhoneNumber());
        return updateUser(user);
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
    public User updateUserAvatar(UUID userId, MultipartFile file) {
        User user = findUserByID(userId);
        checkUserStatus(user);
        if (user.getAvatar() != null) {
            fileService.deleteFile(user.getAvatar().getFileName());
            user.setAvatar(fileService.updateFile(user.getAvatar().getId(), file));
        } else {
            user.setAvatar(fileService.saveFile(file));
        }
        return updateUser(user);
    }

    public User deleteUserAvatar(UUID userId) {
        User user = findUserByID(userId);
        checkUserStatus(user);
        if (user.getAvatar() != null) {
            user.setAvatar(fileService.deleteFile(user.getAvatar().getId()));
        }
        return updateUser(user);
    }

    public Set<UUID> findAllowedChatIds(UUID userID) {
        return userRepository.getAllApplicableChats(userID);
    }

    public User createUser(RegistrationBody body) {
        if (userRepository.isUserExistWithPhoneNumber(body.getPhoneNumber()).isPresent()) {
            throw ErrorUtils.buildException(ApplicationError.USER_EXISTS, "Phone number already registered");
        }
        if (userRepository.isUserExistWithMail(body.getEmail()).isPresent()) {
            throw ErrorUtils.buildException(ApplicationError.USER_EXISTS, "Email already registered");
        }
        if (!verificationService.isVerified(body.getPhoneNumber())) {
            throw ErrorUtils.buildException(ApplicationError.BUSINESS_LOGIC_ERROR, "Phone not verified or verification expired");
        }
        if (new Date().before(body.getBirthDate())) {
            throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Birth date in future not allowed");
        }
        verificationService.deleteRecord(body.getPhoneNumber());
        User user = new User();
        user.setFirstName(body.getFirstName());
        user.setSecondName(body.getSecondName());
        user.setLastName(body.getLastName());
        user.setAccountRank(5f);
        user.setLastRankUpdateDate(null);
        user.setMissSeries(0);
        user.setAttendSeries(0);
        user.setPhoneNumber(body.getPhoneNumber());
        user.setPassword(this.passwordEncoder.encode(body.getPassword()));
        user.setEmail(body.getEmail());
        user.setBirthDate(body.getBirthDate());
        user.setRegistrationDate(new Date());
        user.setRole(Role.USER);
        user.setBlocked(false);
        user.setActive(true);
        user.setBlocked(false);
        User createdUser = userRepository.save(user);
        cacheManager.getCache(USERS_CACHE).put(createdUser.getId(), createdUser);
        return createdUser;
    }

    public boolean isUserExists(String phone) {
        return userRepository.findByPhoneNumber(phone).isPresent();
    }
}
