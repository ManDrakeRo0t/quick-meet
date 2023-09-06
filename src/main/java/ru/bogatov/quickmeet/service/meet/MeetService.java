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
import ru.bogatov.quickmeet.entity.*;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.enums.Icon;
import ru.bogatov.quickmeet.model.enums.MeetStatus;
import ru.bogatov.quickmeet.model.request.*;
import ru.bogatov.quickmeet.model.response.MeetModificationResponse;
import ru.bogatov.quickmeet.model.validation.AccountClassProperties;
import ru.bogatov.quickmeet.repository.meet.MeetRepository;
import ru.bogatov.quickmeet.service.billing.BillingAccountService;
import ru.bogatov.quickmeet.service.file.FileService;
import ru.bogatov.quickmeet.service.user.GuestService;
import ru.bogatov.quickmeet.service.user.UserService;
import ru.bogatov.quickmeet.service.util.MeetUtils;
import ru.bogatov.quickmeet.service.util.RankService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.bogatov.quickmeet.constant.CacheConstants.*;
import static ru.bogatov.quickmeet.model.enums.AccountClass.BASE;

@Service
public class MeetService {

    private final MeetRepository meetRepository;
    private final UserService userService;
    private final GuestService guestService;
    private final JwtProvider jwtProvider;
    private final MeetCategoryService meetCategoryService;
    private final CacheManager cacheManager;
    private final MeetValidationRuleProperties meetValidationRuleProperties;
    private final FileService fileService;
    private final MeetEventSenderService senderService;
    private final RankService rankService;
    private final BillingAccountService billingAccountService;
    private final LocationCacheService locationCacheService;

    public MeetService(MeetRepository meetRepository,
                       UserService userService,
                       GuestService guestService,
                       JwtProvider jwtProvider,
                       MeetCategoryService meetCategoryService,
                       CacheManager cacheManager,
                       MeetValidationRuleProperties meetValidationRuleProperties,
                       FileService fileService,
                       MeetEventSenderService senderService, RankService rankService, BillingAccountService billingAccountService, LocationCacheService locationCacheService) {
        this.meetRepository = meetRepository;
        this.userService = userService;
        this.guestService = guestService;
        this.jwtProvider = jwtProvider;
        this.meetCategoryService = meetCategoryService;
        this.cacheManager = cacheManager;
        this.meetValidationRuleProperties = meetValidationRuleProperties;
        this.fileService = fileService;
        this.senderService = senderService;
        this.rankService = rankService;
        this.billingAccountService = billingAccountService;
        this.locationCacheService = locationCacheService;
    }

    @Transactional
    public MeetModificationResponse createNewMeet(MeetCreationBody body) {
        MeetUtils.validateMeetCreation(body);
        User owner = userService.findUserByID(body.getOwnerId());
        BillingAccount billingAccount = billingAccountService.setBillingAccountClass(
                billingAccountService.getCustomerBillingAccount(body.getOwnerId()),
                body.getTime()
        );
        AccountClassProperties accountProperties = AccountClassProperties.getForClass(billingAccount.getActualClass(), meetValidationRuleProperties);
        if (meetValidationRuleProperties.useRule) {
            Set<Meet> existingMeets = findMeetListWhereUserOwner(owner.getId());
            MeetUtils.validateOwnerClassAndMeetPeriodForCreation(billingAccount, body, owner, existingMeets, meetValidationRuleProperties, accountProperties);
        }
        Meet meet = new Meet();
        setCommonData(meet, body);
        setCategory(meet, body.getCategoryId());
        meet.setIcon(Icon.DEFAULT);
        meet.setHighLighted(accountProperties.isHighlightMeet());
        meet.setIconUpdateType(accountProperties.getIconType());
        meet.setUpdateCount(0);
        meet.setRatingProcessed(false);
        meet.setGuestRatingProcessRequired(true);
        if (body.getLocationId() != null) {
            Location location = locationCacheService.getLocationById(body.getLocationId());
            enrichBusinessMeet(location,body, meet);
        } else {
            enrichBaseMeet(body, meet);
        }
        if (billingAccount.getActualClass() == BASE) {
            meet.setRequiredRank(0);
        } else {
            meet.setRequiredRank(body.getRequiredRank());
        }
        meet.setAdultsOnly(body.isForAdults());
        meet.setMaxPeople(body.getUserAmount());
        meet.setExpectedDuration(body.getExpectedDuration());
        meet.setMeetStatus(MeetStatus.PLANNED);
        meet.setOwner(owner);
        meet.setCurrentPeople(1);
        evictOwnerListCache(body.getOwnerId());
        Meet created = updateInCacheAndReturn(meet);
        senderService.sendMeetCreatedEvent(created.getId());
        return MeetModificationResponse.builder()
                .meet(created)
                .token(jwtProvider.generateTokenForUser(owner))
                .build();
    }

    private void enrichBusinessMeet(Location location, MeetCreationBody body ,Meet toEnrich) {
        toEnrich.setLocation(location);
        toEnrich.setLatitude(location.getLatitude());
        toEnrich.setLongevity(location.getLongevity());
        toEnrich.setAddress(location.getAddress());
    }

    private void enrichBaseMeet(MeetCreationBody body, Meet toEnrich) {
        toEnrich.setLatitude(body.getLatitude());
        toEnrich.setLongevity(body.getLongevity());
        toEnrich.setAddress(body.getAddress());
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
        Meet updated = updateInCacheAndReturn(meet);
        senderService.sendUsedJoinedEvent(updated.getId(), userId);
        return MeetModificationResponse.builder()
                .meet(updated)
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
        senderService.sendMeetAvatarUpdatedEvent(meetId);
        return updateInCacheAndReturn(meet);
    }

    public Meet deleteMeetAvatar(UUID meetId) {
        Meet meet = findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        if (meet.getAvatar() != null) {
            meet.setAvatar(fileService.deleteFile(meet.getAvatar().getId()));
        }
        senderService.sendMeetAvatarUpdatedEvent(meetId);
        return updateInCacheAndReturn(meet);
    }

    @Transactional
    public MeetModificationResponse leaveFromMeet(UUID meetId, UUID userId) {
        Meet meet = this.findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        guestService.deleteGuest(MeetUtils.removeGuest(meet, userId));
        meet = meetRepository.save(meet);
        evictGuestListCache(userId);
        Meet updated = updateInCacheAndReturn(meet);
        senderService.sendUsedLeftEvent(meetId, userId);
        return MeetModificationResponse.builder()
                .meet(updated)
                .token(jwtProvider.generateTokenForUser(userId))
                .build();
    }

    @Transactional
    public Void updateGuest(UUID meetId, UUID guestId, boolean isAttend) {
        cacheManager.getCache(MEET_CACHE).evict(meetId);
        guestService.updateGuest(guestId, isAttend);
        return null;
    }

    public Void updateOwner(UUID meetId, boolean isAttend) {
        Meet meet = findById(meetId);
        meet.setOwnerAttend(isAttend);
        updateInCacheAndReturn(meet);
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
        Meet updated = updateInCacheAndReturn(meet);
        senderService.sendUsedRemovedEvent(meetId, userId);
        return MeetModificationResponse.builder()
                .meet(updated)
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
            } else {
                notFoundInCache.add(uuid);
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
    public Meet updateMeetStatus(UUID id, MeetUpdateStatusBody body, boolean isSystemUpdate) {
        Meet meet = this.findById(id);
        MeetStatus oldStatus = meet.getMeetStatus();
        if (body.getTargetState() != null) {
            MeetUtils.updateState(meet, body.getTargetState());
        }
        if (MeetStatus.FINISHED == body.getTargetState() && !meet.isRatingProcessed()) {
            processRatingUpdate(meet);
            meet.setRatingProcessed(true);
        }
        senderService.sendMeetUpdatedStateEvent(body.getTargetState(), id, isSystemUpdate);
        return updateInCacheAndReturn(meet);
    }

    private void processRatingUpdate(Meet meet) {
        rankService.updateOwnerRank(meet);
        if (meet.isGuestRatingProcessRequired()) {
            meet.getGuests().forEach(guest -> rankService.updateGuestRank(meet, guest));
        }
    }

    @Transactional
    public Meet updateMeet(UUID id, MeetUpdateBody body) {
        MeetUtils.validateMeetUpdate(body);
        Meet meet = this.findById(id);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        if (meetValidationRuleProperties.useRule) {
            Set<Meet> existingMeets = findMeetListWhereUserOwner(meet.getOwner().getId());
            MeetUtils.validateMeetPeriodForUpdate(body, meet, existingMeets, this.meetValidationRuleProperties);
        }
        setCommonData(meet, body);
        int oldDuration = meet.getExpectedDuration();
        if (body.getExpectedDuration() != null) {
            meet.setExpectedDuration(body.getExpectedDuration());
        }
        if (body.getCategoryId() != null) {
            setCategory(meet, body.getCategoryId());
        }
        int updatesCount = senderService.sendMeetUpdatedEvent(id, body, meet.getCategory().getName(), oldDuration);
        rankService.updateOwnerRankForUpdate(meet, updatesCount, this.meetValidationRuleProperties);
        return updateInCacheAndReturn(meet);
    }
    @Transactional
    public void changeStatusPlannedToActive() {
        Set<UUID> meetIdsToStart = meetRepository.getStatusPlannedToActive(LocalDateTime.now());
        meetIdsToStart.parallelStream().forEach(id -> {
            meetRepository.setStatusActive(id);
            senderService.sendMeetUpdatedStateEvent(MeetStatus.ACTIVE, id, true);
            cacheManager.getCache(MEET_CACHE).evict(id);
        });
    }

    @Transactional
    public void changeStatusActiveToFinished(int limit) {
        MeetUpdateStatusBody body = new MeetUpdateStatusBody();
        body.setTargetState(MeetStatus.FINISHED);
        Set<UUID> meetIds = meetRepository.findMeedIdsShouldBeFinished(limit, LocalDateTime.now());
        meetIds.parallelStream().forEach(id -> updateMeetStatus(id, body, true));
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
            meet.setGuestRatingProcessRequired(false);
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

    public Location findLocationById(UUID id) {
        return locationCacheService.getLocationById(id);
    }

}
