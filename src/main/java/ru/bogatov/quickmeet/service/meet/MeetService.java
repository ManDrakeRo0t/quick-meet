package ru.bogatov.quickmeet.service.meet;

import io.netty.util.internal.StringUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.bogatov.quickmeet.config.application.MeetValidationRuleProperties;
import ru.bogatov.quickmeet.config.security.JwtProvider;
import ru.bogatov.quickmeet.entity.Guest;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.enums.MeetStatus;
import ru.bogatov.quickmeet.model.request.*;
import ru.bogatov.quickmeet.model.response.MeetModificationResponse;
import ru.bogatov.quickmeet.repository.meet.MeetRepository;
import ru.bogatov.quickmeet.service.file.FileService;
import ru.bogatov.quickmeet.service.user.GuestService;
import ru.bogatov.quickmeet.service.user.UserService;
import ru.bogatov.quickmeet.service.util.CityService;
import ru.bogatov.quickmeet.service.util.MeetUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.bogatov.quickmeet.constant.CacheConstants.*;
import static ru.bogatov.quickmeet.constant.UserConstants.*;

@Service
public class MeetService {

    private final MeetRepository meetRepository;
    private final UserService userService;
    private final CityService cityService;
    private final GuestService guestService;
    private final JwtProvider jwtProvider;
    private final MeetCategoryService meetCategoryService;
    private final CacheManager cacheManager;
    private final MeetValidationRuleProperties meetValidationRuleProperties;

    private final FileService fileService;

    public MeetService(MeetRepository meetRepository, UserService userService, CityService cityService, GuestService guestService, JwtProvider jwtProvider, MeetCategoryService meetCategoryService, CacheManager cacheManager, MeetValidationRuleProperties meetValidationRuleProperties, FileService fileService) {
        this.meetRepository = meetRepository;
        this.userService = userService;
        this.cityService = cityService;
        this.guestService = guestService;
        this.jwtProvider = jwtProvider;
        this.meetCategoryService = meetCategoryService;
        this.cacheManager = cacheManager;
        this.meetValidationRuleProperties = meetValidationRuleProperties;
        this.fileService = fileService;
    }

    @Transactional
    public MeetModificationResponse createNewMeet(MeetCreationBody body) {
        MeetUtils.validateMeetCreation(body);
        User owner = userService.findUserByID(body.getOwnerId());
        if (meetValidationRuleProperties.useRule) {
            Set<Meet> existingMeets = findMeetListWhereUserOwner(owner.getId());
            MeetUtils.validateOwnerClassAndMeetPeriodForCreation(body, owner, existingMeets, meetValidationRuleProperties);
        }
        Meet meet = new Meet();
        setCity(body, owner, meet);
        setCommonData(meet, body);
        setCategory(meet, body.getCategoryId());
        meet.setLatitude(body.getLatitude());
        meet.setRatingProcessed(false);
        meet.setAddress(body.getAddress());
        meet.setLongevity(body.getLongevity());
        meet.setAttendRequired(body.isAttendRequired());
        meet.setMaxPeople(body.getUserAmount());
        meet.setExpectedDuration(body.getExpectedDuration());
        meet.setMeetStatus(MeetStatus.PLANNED);
        meet.setRank(owner.getAccountRank());
        meet.setOwner(owner);
        meet.setCurrentPeople(1);
        evictOwnerListCache(body.getOwnerId());
        return MeetModificationResponse.builder()
                .meet(updateInCacheAndReturn(meet))
                .token(jwtProvider.generateTokenForUser(owner))
                .build();
    }

    @Transactional
    public MeetModificationResponse joinToMeet(UUID meetId, UUID userId) {
        Meet meet = this.findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        MeetUtils.checkIsFreeAndThrow(meet);
        MeetUtils.checkIsGuestApplicable(meet, userId);
        Guest guest = guestService.createGuest(meet, userId);
        MeetUtils.addGuest(meet, guest);
        evictGuestListCache(userId);
        return MeetModificationResponse.builder()
                .meet(updateInCacheAndReturn(meet))
                .token(jwtProvider.generateTokenForUser(userId))
                .build();
    }

    public Meet updateMeetAvatar(UUID meetId, MultipartFile file) {
        Meet meet = findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        if (meet.getAvatar() != null) {
            fileService.deleteFile(meet.getAvatar().getFileName());
            meet.setAvatar(fileService.updateFile(meet.getAvatar().getId(), file));
        } else {
            meet.setAvatar(fileService.saveFile(file));
        }
        return updateInCacheAndReturn(meet);
    }

    public Meet deleteMeetAvatar(UUID meetId) {
        Meet meet = findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        if (meet.getAvatar() != null) {
            meet.setAvatar(fileService.deleteFile(meet.getAvatar().getId()));
        }
        return updateInCacheAndReturn(meet);
    }

    @Transactional
    public MeetModificationResponse leaveFromMeet(UUID meetId, UUID userId) {
        Meet meet = this.findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        guestService.deleteGuest(MeetUtils.removeGuest(meet, userId));
        meet = meetRepository.save(meet);
        evictGuestListCache(userId);
        return MeetModificationResponse.builder()
                .meet(updateInCacheAndReturn(meet))
                .token(jwtProvider.generateTokenForUser(userId))
                .build();
    }

    @Transactional
    public Void updateGuest(UUID meetId, UUID guestId, boolean isAttend) {
        cacheManager.getCache(MEET_CACHE).evict(meetId);
        guestService.updateGuest(guestId, isAttend);
        return null;
    }

    @Transactional
    public MeetModificationResponse removeUserFromMeet(UUID meetId, UUID userId) {
        Meet meet = this.findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        MeetUtils.removeGuest(meet, userId);
        MeetUtils.addUserToBlackList(meet, userId);
        meet = meetRepository.save(meet);
        evictGuestListCache(userId);
        return MeetModificationResponse.builder()
                .meet(updateInCacheAndReturn(meet))
                .build();
    }

    private Meet updateInCacheAndReturn(Meet meet) {
        Meet updatedMeet = meetRepository.save(meet);
        cacheManager.getCache(MEET_CACHE).put(updatedMeet.getId(), updatedMeet);
        return updatedMeet;
    }

    public Set<Meet> search(SearchMeetBody body) {
        Pair<Pair<Double, Double>, Pair<Double, Double>> border =
                MeetUtils.calculateBorder(body.getLatitude(), body.getLongevity(), body.getRadius());
        List<String> stringStatuses = body.getStatuses().stream().map(MeetStatus::getValue).collect(Collectors.toList());
        Set<UUID> foundMeetIds = meetRepository.searchMeet(stringStatuses,
                body.getCategories(),
                border.getFirst().getFirst(),
                border.getSecond().getFirst(),
                border.getFirst().getSecond(),
                border.getSecond().getSecond(),
                body.getDateFrom(),
                body.getDateTo()
        );
        return findMeetListByIds(foundMeetIds);
    }


    public Set<Meet> findMeetListWhereUserGuest(UUID userId) {
        Set<UUID> meetIds = findMeetIdsListWhereUserGuest(userId);
        return findMeetListByIds(meetIds);
    }

    public Set<Meet> findMeetListWhereUserOwner(UUID userId) {
        Set<UUID> meetIds = findMeetIdsListWhereUserOwner(userId);
        return findMeetListByIds(meetIds);
    }
    @Cacheable(value = MEET_LIST_GUEST_CACHE, key = "#userId")
    public Set<UUID> findMeetIdsListWhereUserGuest(UUID userId) {
        return meetRepository.findMeetsIdWhereUserGuest(userId);
    }
    @Cacheable(value = MEET_LIST_OWNER_CACHE, key = "#userId")
    public Set<UUID> findMeetIdsListWhereUserOwner(UUID userId) {
        return meetRepository.findMeetsIdWhereUserOwner(userId);
    }

    private Set<Meet> findMeetListByIds(Set<UUID> ids) {
        Set<Meet> result = new HashSet<>();
        Set<UUID> notFoundInCache = new HashSet<>();
        Cache meetCache = cacheManager.getCache(MEET_CACHE);
        ids.forEach(uuid -> {
            if (meetCache != null) {
                Meet fromCache = meetCache.get(uuid, Meet.class);
                if (fromCache != null) {
                    result.add(fromCache);
                } else {
                    notFoundInCache.add(uuid);
                }
            }
        });
        List<Meet> foundInDb = meetRepository.findAllById(notFoundInCache);
        foundInDb.forEach(meet -> {
            meetCache.put(meet.getId(), meet);
            result.add(meet);
        });
        return result;
    }

    @Transactional
    public Meet updateMeetStatus(UUID id, MeetUpdateStatusBody body) {
        Meet meet = this.findById(id);
        if (body.getTargetState() != null) {
            MeetUtils.updateState(meet, body.getTargetState());
        }
        if (MeetStatus.FINISHED == body.getTargetState() && !meet.isRatingProcessed()) {
            processRatingUpdate(meet);
            meet.setRatingProcessed(true);
        }
        return updateInCacheAndReturn(meet);
    }

    private void processRatingUpdate(Meet meet) {
        User owner = meet.getOwner();
        updateOwnerRank(meet, owner);
        meet.getGuests().forEach(guest -> updateGuestRank(meet, guest));
    }

    private void updateOwnerRank(Meet meet, User owner) {
        float currentRank = owner.getAccountRank();
        if (currentRank == MAX_RANK) {
            return;
        }
        float updatedRank = currentRank;
        if (meet.getGuests() != null && !meet.getGuests().isEmpty()) {
            if (meet.isAttendRequired()) {
                long attendedGuests = meet.getGuests().stream().filter(Guest::isAttend).count();
                updatedRank += attendedGuests * RANK_UPDATE_DELTA;
            } else {
                updatedRank += RANK_UPDATE_DELTA;
            }
        }
        updatedRank = Math.min(updatedRank, MAX_RANK);
        if (updatedRank != currentRank) {
            owner.setAccountRank(updatedRank);
            userService.updateUser(owner);
        }
    }

    private void updateGuestRank(Meet meet, Guest guest) {
        User user = userService.findUserByID(guest.getUserId());
        int attendSeries = user.getAttendSeries();
        int missSeries = user.getMissSeries();
        float updatedRank = user.getAccountRank();
        if (guest.isAttend()) {
            attendSeries += 1;
            missSeries = 0;
        } else {
            missSeries +=1;
            attendSeries = 0;
        }
        if (meet.isAttendRequired() && !guest.isAttend()) {
            updatedRank -= missSeries * RANK_UPDATE_DELTA;
            if (updatedRank < MIN_RANK) {
                updatedRank = MIN_RANK;
            }
         } else if (guest.isAttend()) {
            updatedRank += attendSeries * RANK_UPDATE_DELTA;
            if (updatedRank > MAX_RANK) {
                updatedRank = MAX_RANK;
            }
        }
        user.setAccountRank(updatedRank);
        user.setAttendSeries(attendSeries);
        user.setMissSeries(missSeries);
        userService.updateUser(user);
    }

    @Transactional
    public Meet updateMeet(UUID id, MeetUpdateBody body) {
        MeetUtils.validateMeetUpdate(body);
        Meet meet = this.findById(id);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        setCommonData(meet, body);
        if (meetValidationRuleProperties.useRule) {
            Set<Meet> existingMeets = findMeetListWhereUserOwner(meet.getOwner().getId());
            MeetUtils.validateMeetPeriodForUpdate(body, meet, existingMeets, this.meetValidationRuleProperties);
        }
        meet.setExpectedDuration(body.getExpectedDuration());
        if (body.getCategoryId() != null) {
            setCategory(meet, body.getCategoryId());
        }
        return updateInCacheAndReturn(meet);
    }
    @Transactional
    public void changeStatusPlannedToActive() {
        meetRepository.updateStatusPlannedToActive(LocalDateTime.now());
        cacheManager.getCache(MEET_CACHE).clear();
    }

    @Transactional
    public void changeStatusActiveToFinished(int limit) {
        MeetUpdateStatusBody body = new MeetUpdateStatusBody();
        body.setTargetState(MeetStatus.FINISHED);
        Set<UUID> meetIds = meetRepository.findMeedIdsShouldBeFinished(limit, LocalDateTime.now());
        meetIds.parallelStream().forEach(id -> updateMeetStatus(id, body));
    }

    private void setCity(MeetCreationBody body, User owner, Meet meet) {
        if (body.getCityId() != null) {
            meet.setCity(cityService.getById(body.getCityId()));
        }
        meet.setCity(owner.getCity());
    }

    private void setCommonData(Meet meet, MeetCommonData body) {
        if (!StringUtil.isNullOrEmpty(body.getName())) {
            meet.setName(body.getName());
        }
        if (!StringUtil.isNullOrEmpty(body.getDescription())) {
            meet.setDescription(body.getDescription());
        }
        if (body.getTime() != null) {
            meet.setDateTime(body.getTime());
        }
    }

    private void setCategory(Meet meet, UUID categoryId) {
        meet.setCategory(meetCategoryService.findById(categoryId));
    }

    private void evictOwnerListCache(UUID userId) {
        cacheManager.getCache(MEET_LIST_OWNER_CACHE).evict(userId);
    }

    private void evictGuestListCache(UUID userId) {
        cacheManager.getCache(MEET_LIST_GUEST_CACHE).evict(userId);
    }

    @Cacheable(value = MEET_CACHE, key = "#id")
    public Meet findById(UUID id) {
        return meetRepository.findById(id).orElseThrow(() -> ErrorUtils.buildException(ApplicationError.DATA_NOT_FOUND_ERROR, "Meet not found"));
    }

}
