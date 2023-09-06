package ru.bogatov.quickmeet.service.meet;

import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entity.Location;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.repository.meet.LocationRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static ru.bogatov.quickmeet.constant.CacheConstants.LOCATION_CACHE;
import static ru.bogatov.quickmeet.constant.CacheConstants.MEET_CACHE;

@Service
@AllArgsConstructor //todo refactor
public class LocationCacheService {

    private final LocationRepository locationRepository;

    private final CacheManager cacheManager;

    @Cacheable(value = LOCATION_CACHE, key = "#locationId")
    public Location getLocationById(UUID locationId) {
        return locationRepository.findById(locationId).orElseThrow(() -> ErrorUtils.buildException(ApplicationError.DATA_NOT_FOUND_ERROR, "Location not found"));
    }

    public Set<Location> search(double lat_min, double lat_max, double lon_min, double lon_max) {
        return findByIds(locationRepository.search(lat_min, lat_max, lon_min, lon_max));
    }

    public Set<Location> findByIds(Set<UUID> ids) {
        Set<Location> result = new HashSet<>();
        Set<UUID> notFoundInCache = new HashSet<>();
        Cache locationCache = cacheManager.getCache(LOCATION_CACHE);
        ids.forEach(uuid -> {
            if (locationCache != null) {
                Location fromCache = locationCache.get(uuid, Location.class);
                if (fromCache != null) {
                    result.add(fromCache);
                } else {
                    notFoundInCache.add(uuid);
                }
            } else {
                notFoundInCache.add(uuid);
            }
        });
        List<Location> foundInDb = locationRepository.findAllById(notFoundInCache);
        foundInDb.forEach(location -> {
            locationCache.put(location.getId(), location);
            result.add(location);
        });
        return result;
    }

}
