package ru.bogatov.quickmeet.service.meet;

import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.bogatov.quickmeet.entity.*;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.enums.MeetStatus;
import ru.bogatov.quickmeet.model.request.CreateBannerBody;
import ru.bogatov.quickmeet.model.request.CreateLocationBody;
import ru.bogatov.quickmeet.model.request.MeetUpdateStatusBody;
import ru.bogatov.quickmeet.model.request.UpdateLocationBody;
import ru.bogatov.quickmeet.repository.meet.BannerRepository;
import ru.bogatov.quickmeet.repository.meet.LocationRepository;
import ru.bogatov.quickmeet.service.billing.BillingAccountService;
import ru.bogatov.quickmeet.service.file.FileService;
import ru.bogatov.quickmeet.service.user.UserService;

import java.time.LocalDateTime;
import java.util.*;

import static ru.bogatov.quickmeet.constant.CacheConstants.LOCATION_CACHE;

@Service
@AllArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    private final BannerRepository bannerRepository;

    private final CacheManager cacheManager;

    private final BillingAccountService billingAccountService;

    private final UserService userService;

    private final FileService fileService;

    private final LocationCacheService locationCacheService;

    private final MeetService meetService;
    private final int BANNER_LIMIT = 10;
    @Transactional
    public Location createLocation(CreateLocationBody body) {
        BillingAccount billingAccount = billingAccountService.setBillingAccountClass(
                billingAccountService.getCustomerBillingAccount(body.getUserId()),
                LocalDateTime.now()
        );
        validateBillingAccount(billingAccount);

        User owner = userService.findUserByID(body.getUserId());

        Location location = new Location();
        location.setAddress(body.getAddress());
        location.setLatitude(body.getLatitude());
        location.setLongevity(body.getLongevity());
        location.setName(body.getName());
        location.setHidden(false);
        location.setDescription(body.getDescription());
        location.setAvailableTill(billingAccount.getBusinessEndTime());
        location.setOwner(owner);

        billingAccount.setLocationsAmount(billingAccount.getLocationsAmount() + 1);
        billingAccountService.saveAndUpdateInCache(billingAccount);
        return saveAndUpdateInCache(location);
    }

    public void validateBillingAccount(BillingAccount account) {
        if (account == null) {
            throw ErrorUtils.buildException(ApplicationError.BILLING_ACCOUNT_ERROR, "Account not exists");
        }

        if (account.getActualClass() != AccountClass.BUSINESS) {
            throw ErrorUtils.buildException(ApplicationError.BILLING_ACCOUNT_ERROR, "Account class not BUSINESS");
        }

        if (account.getLocationsAmount() + 1 > account.getMaxAmount()) {
            throw ErrorUtils.buildException(ApplicationError.BILLING_ACCOUNT_ERROR, "Reached max locations capacity limit");
        }
    }

    public Location saveAndUpdateInCache(Location location) {
        Location updatedLocation = locationRepository.save(location);
        cacheManager.getCache(LOCATION_CACHE).put(updatedLocation.getId(), updatedLocation);
        return updatedLocation;
    }

    public Location updateLocation(UUID id, UpdateLocationBody body) {
        Location location = locationCacheService.getLocationById(id);
        if (!StringUtil.isNullOrEmpty(body.getName())) {
            location.setName(body.getName());
        }
        if (!StringUtil.isNullOrEmpty(body.getDescription())) {
            location.setDescription(body.getDescription());
        }
        return saveAndUpdateInCache(location);
    }

    public Location updateLocationAvatar(UUID id, MultipartFile file) {
        Location location = locationCacheService.getLocationById(id);
        if (location.getAvatar() != null) {
            fileService.deleteFile(location.getAvatar().getFileName());
            location.setAvatar(fileService.updateFile(location.getAvatar().getId(), file));
        } else {
            location.setAvatar(fileService.saveFile(file));
        }
        return saveAndUpdateInCache(location);
    }

    public Location deleteLocationAvatar(UUID id) {
        Location location = locationCacheService.getLocationById(id);
        if (location.getAvatar() != null) {
            location.setAvatar(fileService.deleteFile(location.getAvatar().getId()));
        }
        return saveAndUpdateInCache(location);
    }

    public Void deleteLocation(UUID id, UUID userId) {
        Location location = locationCacheService.getLocationById(id);
        if (location.isHidden()) {
            throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Location already deleted");
        }
        location.setHidden(true);

        BillingAccount billingAccount = billingAccountService.getCustomerBillingAccount(userId);
        billingAccount.setLocationsAmount(billingAccount.getLocationsAmount() - 1);
        billingAccountService.saveAndUpdateInCache(billingAccount);

        MeetUpdateStatusBody updateStatusBody = new MeetUpdateStatusBody();
        updateStatusBody.setTargetState(MeetStatus.CANCELED);

        meetService.findMeetsByLocationId(location.getId())
                .stream().filter(meet -> meet.getMeetStatus() == MeetStatus.PLANNED)
                .forEach(meet -> meetService.updateMeetStatus(meet.getId(), updateStatusBody, true));


        saveAndUpdateInCache(location);
        return null;
    }

    public Location createBanner(UUID locationId, CreateBannerBody body) {
        Location location = locationCacheService.getLocationById(locationId);
        List<Banner> banners = location.getBanners();
        if (banners != null && !banners.isEmpty()) {
            if (banners.size() > BANNER_LIMIT) {
                throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Reached max banners capacity limit");
            }
        } else {
            location.setBanners(new ArrayList<>());
        }
        Banner banner = new Banner();
        banner.setName(body.getName());
        banner.setDescription(body.getDescription());
        banner.setLocation(location);
        banner = bannerRepository.save(banner);
        location.getBanners().add(banner);
        return saveAndUpdateInCache(location);
    }

    public void deleteFromCache(UUID locationId) {
        cacheManager.getCache(LOCATION_CACHE).evict(locationId);
    }

    @Transactional
    public Location updateBanner(UUID locationId, UUID bannerId, CreateBannerBody body) {
        Location location = locationCacheService.getLocationById(locationId);
        Banner foundedBanner = findBanner(location, bannerId);
        if (!StringUtil.isNullOrEmpty(body.getName())) {
            foundedBanner.setName(body.getName());
        }
        if (!StringUtil.isNullOrEmpty(body.getDescription())) {
            foundedBanner.setDescription(body.getDescription());
        }
        bannerRepository.save(foundedBanner);
        deleteFromCache(locationId);
        return locationCacheService.getLocationById(locationId);
    }

    public Location deleteBanner(UUID locationId, UUID bannerId) {
        Location location = locationCacheService.getLocationById(locationId);
        Banner foundedBanner = findBanner(location, bannerId);
        UUID avatarId = null;
        if (foundedBanner.getAvatar() != null) {
            avatarId = foundedBanner.getAvatar().getId();
        }
        foundedBanner.setAvatar(null);
        bannerRepository.delete(foundedBanner);
        fileService.deleteFileWithEntity(avatarId);
        deleteFromCache(locationId);
        return locationCacheService.getLocationById(locationId);
    }

    public Banner findBanner(Location location, UUID bannerId) {
        return location.getBanners()
                .stream()
                .filter(banner -> banner.getId().equals(bannerId))
                .findFirst()
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Banner not found"));
    }

    public Location updateBannerAvatar(UUID locationId, UUID bannerId, MultipartFile file) {
        Location location = locationCacheService.getLocationById(locationId);
        Banner banner = findBanner(location, bannerId);
        if (banner.getAvatar() != null) {
            fileService.deleteFile(banner.getAvatar().getFileName());
            banner.setAvatar(fileService.updateFile(banner.getAvatar().getId(), file));
        } else {
            banner.setAvatar(fileService.saveFile(file));
        }
        bannerRepository.save(banner);
        deleteFromCache(locationId);
        return locationCacheService.getLocationById(locationId);
    }

    public Location deleteBannerAvatar(UUID locationId, UUID bannerId) {
        Location location = locationCacheService.getLocationById(locationId);
        Banner banner = findBanner(location, bannerId);
        if (banner.getAvatar() != null) {
            banner.setAvatar(fileService.deleteFile(banner.getAvatar().getId()));
        }
        bannerRepository.save(banner);
        deleteFromCache(locationId);
        return locationCacheService.getLocationById(locationId);
    }

    public Set<Meet> findMeetsUnderLocation(UUID locationId) {
        return meetService.findMeetsByLocationId(locationId);
    }

}
