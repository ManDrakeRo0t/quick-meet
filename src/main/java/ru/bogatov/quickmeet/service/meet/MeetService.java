package ru.bogatov.quickmeet.service.meet;

import io.netty.util.internal.StringUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bogatov.quickmeet.config.security.JwtProvider;
import ru.bogatov.quickmeet.entity.Guest;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.enums.MeetStatus;
import ru.bogatov.quickmeet.model.request.MeetCommonData;
import ru.bogatov.quickmeet.model.request.MeetCreationBody;
import ru.bogatov.quickmeet.model.request.MeetUpdateBody;
import ru.bogatov.quickmeet.model.request.SearchMeetBody;
import ru.bogatov.quickmeet.model.response.MeetModificationResponse;
import ru.bogatov.quickmeet.repository.meet.MeetRepository;
import ru.bogatov.quickmeet.service.user.GuestService;
import ru.bogatov.quickmeet.service.user.UserService;
import ru.bogatov.quickmeet.service.util.CityService;
import ru.bogatov.quickmeet.service.util.MeetUtils;

import java.util.*;
import java.util.stream.Collectors;

import static ru.bogatov.quickmeet.constant.CacheConstants.*;

@Service
public class MeetService {

    private final MeetRepository meetRepository;
    private final UserService userService;
    private final CityService cityService;
    private final GuestService guestService;
    private final JwtProvider jwtProvider;
    private final MeetCategoryService meetCategoryService;
    private final CacheManager cacheManager;

    public MeetService(MeetRepository meetRepository, UserService userService, CityService cityService, GuestService guestService, JwtProvider jwtProvider, MeetCategoryService meetCategoryService, CacheManager cacheManager) {
        this.meetRepository = meetRepository;
        this.userService = userService;
        this.cityService = cityService;
        this.guestService = guestService;
        this.jwtProvider = jwtProvider;
        this.meetCategoryService = meetCategoryService;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public MeetModificationResponse createNewMeet(MeetCreationBody body) {
        MeetUtils.validateMeetCreation(body);
        Meet meet = new Meet();
        User owner = userService.findUserByID(body.getOwnerId());
        setCity(body, owner, meet);
        setCommonData(meet, body);
        setCategory(meet, body.getCategoryId());
        meet.setLatitude(body.getLatitude());
        meet.setRatingProcessed(false);
        meet.setAddress(body.getAddress());
        meet.setLongevity(body.getLongevity());
        meet.setMaxPeople(body.getUserAmount());
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
        return meetRepository.findMeetsIdWhereUserGuest(userId);
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
    public Meet updateMeet(UUID id, MeetUpdateBody body) {
        MeetUtils.validateMeetUpdate(body);
        Meet meet = this.findById(id);
        if (body.getStatus() != null) {
            MeetUtils.updateState(meet, body.getStatus());
        }
        setCommonData(meet, body);
        if (body.getCategoryId() != null) {
            setCategory(meet, body.getCategoryId());
        }
        return updateInCacheAndReturn(meet);
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
