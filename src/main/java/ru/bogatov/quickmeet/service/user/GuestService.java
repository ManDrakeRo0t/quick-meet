package ru.bogatov.quickmeet.service.user;

import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entity.Guest;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.repository.userdata.GuestRepository;

import java.util.List;
import java.util.UUID;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public Guest createGuest(Meet meet, UUID userId) {
        Guest guest = new Guest();
        guest.setMeet(meet);
        guest.setUserId(userId);
        guest.setAttend(false);
        return guestRepository.save(guest);
    }

    private Guest findById(UUID id) {
        return guestRepository.findById(id).orElseThrow( () -> ErrorUtils.buildException(ApplicationError.DATA_NOT_FOUND_ERROR));
    }

    public void updateGuest(UUID id, boolean isAttend) {
        guestRepository.setAttend(isAttend, id);
    }

    public void deleteGuest(UUID id) {
        guestRepository.deleteById(id);
    }


}
