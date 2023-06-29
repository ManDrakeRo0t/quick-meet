package ru.bogatov.quickmeet.service.meet;

import io.netty.util.internal.StringUtil;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MeetService {

    private final MeetRepository meetRepository;
    private final UserService userService;
    private final CityService cityService;
    private final GuestService guestService;
    private final JwtProvider jwtProvider;
    private final MeetCategoryService meetCategoryService;

    public MeetService(MeetRepository meetRepository, UserService userService, CityService cityService, GuestService guestService, JwtProvider jwtProvider, MeetCategoryService meetCategoryService) {
        this.meetRepository = meetRepository;
        this.userService = userService;
        this.cityService = cityService;
        this.guestService = guestService;
        this.jwtProvider = jwtProvider;
        this.meetCategoryService = meetCategoryService;
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
        return MeetModificationResponse.builder()
                .meet(meetRepository.save(meet))
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
        return MeetModificationResponse.builder()
                .meet(meetRepository.save(meet))
                .token(jwtProvider.generateTokenForUser(userId))
                .build();
    }

    @Transactional
    public MeetModificationResponse leaveFromMeet(UUID meetId, UUID userId) {
        Meet meet = this.findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        guestService.deleteGuest(MeetUtils.removeGuest(meet, userId));
        meet = meetRepository.save(meet);
        return MeetModificationResponse.builder()
                .meet(meetRepository.save(meet))
                .token(jwtProvider.generateTokenForUser(userId))
                .build();
    }

    @Transactional
    public Void updateGuest(UUID meetId, UUID guestId, boolean isAttend) {
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
        return MeetModificationResponse.builder()
                .meet(meetRepository.save(meet))
                .build();
    }

    public List<Meet> search(SearchMeetBody body) {
        Pair<Pair<Double, Double>, Pair<Double, Double>> border =
                MeetUtils.calculateBorder(body.getLatitude(), body.getLongevity(), body.getRadius());
        List<String> stringStatuses = body.getStatuses().stream().map(MeetStatus::getValue).collect(Collectors.toList());
        return meetRepository.searchMeet(stringStatuses,
                body.getCategories(),
                border.getFirst().getFirst(),
                border.getSecond().getFirst(),
                border.getFirst().getSecond(),
                border.getSecond().getSecond(),
                body.getDateFrom(),
                body.getDateTo()
        );
    }

    public List<Meet> findMeetListWhereUserGuest(UUID userId) {
        return meetRepository.findMeetsIdWhereUserGuest(userId);
    }

    public List<Meet> findMeetListWhereUserOwner(UUID userId) {
        return meetRepository.findMeetsIdWhereUserOwner(userId);
    }

    @Transactional
    public Meet updateMeet(UUID id, MeetUpdateBody body) {
        Meet meet = this.findById(id);
        if (body.getStatus() != null) {
            MeetUtils.updateState(meet, body.getStatus());
        }
        setCommonData(meet, body);
        if (body.getCategoryId() != null) {
            setCategory(meet, body.getCategoryId());
        }
        return meetRepository.save(meet);
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

    public Meet findById(UUID id) {
        return meetRepository.findById(id).orElseThrow(() -> ErrorUtils.buildException(ApplicationError.DATA_NOT_FOUND_ERROR, "Meet not found"));
    }

}
