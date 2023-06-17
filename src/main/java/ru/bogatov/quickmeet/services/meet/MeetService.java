package ru.bogatov.quickmeet.services.meet;

import io.netty.util.internal.StringUtil;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bogatov.quickmeet.entities.Meet;
import ru.bogatov.quickmeet.entities.User;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.enums.MeetStatus;
import ru.bogatov.quickmeet.model.request.MeetCommonData;
import ru.bogatov.quickmeet.model.request.MeetCreationBody;
import ru.bogatov.quickmeet.model.request.MeetUpdateBody;
import ru.bogatov.quickmeet.model.request.SearchMeetBody;
import ru.bogatov.quickmeet.repositories.meet.MeetRepository;
import ru.bogatov.quickmeet.services.user.UserService;
import ru.bogatov.quickmeet.services.util.CityService;
import ru.bogatov.quickmeet.services.util.MeetUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MeetService {

    MeetRepository meetRepository;
    UserService userService;
    CityService cityService;
    MeetCategoryService meetCategoryService;

    public MeetService(MeetRepository meetRepository, UserService userService, CityService cityService, MeetCategoryService meetCategoryService) {
        this.meetRepository = meetRepository;
        this.userService = userService;
        this.cityService = cityService;
        this.meetCategoryService = meetCategoryService;
    }

    @Transactional
    public Meet createNewMeet(MeetCreationBody body) {
        Meet meet = new Meet(); //todo more validations
        User owner = userService.findUserByID(body.getOwnerId());
        setCity(body, owner, meet);
        setCommonData(meet, body);
        setCategory(meet, body.getCategoryId());
        meet.setLatitude(body.getLatitude());
        meet.setAddress(body.getAddress());
        meet.setLongevity(body.getLongevity());
        meet.setMaxPeople(body.getUserAmount());
        meet.setMeetStatus(MeetStatus.PLANNED);
        meet.setRank(owner.getAccountRank());
        meet.setOwner(owner);
        meet.setCurrentPeople(1);
        return meetRepository.save(meet);
    }

    @Transactional
    //todo  if you already guest cant join
    public Meet joinToMeet(UUID meetId, UUID guestId) {
        Meet meet = this.findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        MeetUtils.checkIsFreeAndThrow(meet);
        User guest = userService.findUserByID(guestId);
        MeetUtils.checkIsGuestApplicable(meet, guest);
        MeetUtils.addGuest(meet, guest);
        meet = meetRepository.save(meet);
        return meet;
    }

    @Transactional
    public Meet leaveFromMeet(UUID meetId, UUID guestId) {
        Meet meet = this.findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        User guest = userService.findUserByID(guestId);
        MeetUtils.removeGuest(meet, guest);
        meet = meetRepository.save(meet);
        return meet;
    }

    @Transactional
    // todo check removes
    public Meet removeUserFromMeet(UUID meetId, UUID guestId) {
        Meet meet = this.findById(meetId);
        MeetUtils.checkStatusAndThrow(meet, MeetStatus.PLANNED);
        User guest = userService.findUserByID(guestId);
        MeetUtils.removeGuest(meet, guest);
        MeetUtils.addUserToBlackList(meet, guest);
        meet = meetRepository.save(meet);
        return meet;
    }

    public List<Meet> search(SearchMeetBody body) {
        Pair<Pair<Double, Double>, Pair<Double, Double>> border =
                MeetUtils.calculateBorder(body.getLatitude(), body.getLongevity(), body.getRadius());
        System.out.println(border.getFirst().getFirst() + ", " + border.getFirst().getSecond());
        System.out.println(border.getSecond().getFirst() + ", " + border.getSecond().getSecond());
        List<String> stringStatuses = body.getStatuses().stream().map(MeetStatus::getValue).collect(Collectors.toList());
        return meetRepository.searchMeet(stringStatuses,
                body.getCategories(),
                border.getFirst().getFirst(),
                border.getSecond().getFirst(),
                border.getFirst().getSecond(),
                border.getSecond().getSecond()
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
