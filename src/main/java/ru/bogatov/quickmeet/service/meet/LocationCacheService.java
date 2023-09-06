package ru.bogatov.quickmeet.service.meet;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entity.Location;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.repository.meet.LocationRepository;

import java.util.Set;
import java.util.UUID;

import static ru.bogatov.quickmeet.constant.CacheConstants.LOCATION_CACHE;

@Service
@AllArgsConstructor //todo refactor
public class LocationCacheService {

    private final LocationRepository locationRepository;

    @Cacheable(value = LOCATION_CACHE, key = "#locationId")
    public Location getLocationById(UUID locationId) {
        return locationRepository.findById(locationId).orElseThrow(() -> ErrorUtils.buildException(ApplicationError.DATA_NOT_FOUND_ERROR, "Location not found"));
    }

    public Set<Location> search() {
        return null;
    }

}
