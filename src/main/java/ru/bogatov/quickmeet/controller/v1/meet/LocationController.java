package ru.bogatov.quickmeet.controller.v1.meet;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.entity.Location;
import ru.bogatov.quickmeet.model.request.CreateBannerBody;
import ru.bogatov.quickmeet.model.request.CreateLocationBody;
import ru.bogatov.quickmeet.model.request.UpdateLocationBody;
import ru.bogatov.quickmeet.service.meet.LocationService;

import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.MEET_MANAGEMENT + RouteConstants.LOCATION)
@AllArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("")
    public ResponseEntity<Location> createLocation(@RequestBody CreateLocationBody body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createLocation(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable UUID id, @RequestBody UpdateLocationBody body) {
        return ResponseEntity.ok(locationService.updateLocation(id, body));
    }

    @PreAuthorize("@customSecurityRules.isLocationOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Location> updateMeetAvatar(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(locationService.updateLocationAvatar(id, file));
    }

    @PreAuthorize("@customSecurityRules.isLocationOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @DeleteMapping(path = "/{id}/avatar")
    public ResponseEntity<Location> deleteMeetAvatar(@PathVariable UUID id) {
        return ResponseEntity.ok(locationService.deleteLocationAvatar(id));
    }

    @PreAuthorize("@customSecurityRules.isLocationOwnerRequest(#locationId) || hasAnyAuthority('ADMIN')")
    @DeleteMapping(path = "/{locationId}/user/{userId}")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID locationId, @PathVariable UUID userId) {
        return ResponseEntity.ok(locationService.deleteLocation(locationId, userId));
    }
    @PreAuthorize("@customSecurityRules.isLocationOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PostMapping("/{id}/banner")
    public ResponseEntity<Location> createBanner(@PathVariable UUID id, @RequestBody CreateBannerBody body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createBanner(id, body));
    }
    @PreAuthorize("@customSecurityRules.isLocationOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/{id}/banner/{bannerId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Location> updateBannerAvatar(@PathVariable UUID id, @PathVariable UUID bannerId, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.updateBannerAvatar(id, bannerId, file));
    }

    @PreAuthorize("@customSecurityRules.isLocationOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @DeleteMapping(path = "/{id}/banner/{bannerId}/avatar")
    public ResponseEntity<Location> deleteBannerAvatar(@PathVariable UUID id, @PathVariable UUID bannerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.deleteBannerAvatar(id, bannerId));
    }
    @PreAuthorize("@customSecurityRules.isLocationOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PutMapping("/{id}/banner/{bannerId}")
    public ResponseEntity<Location> updateBanner(@PathVariable UUID id, @PathVariable UUID bannerId, @RequestBody CreateBannerBody body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.updateBanner(id, bannerId ,body));
    }
    @PreAuthorize("@customSecurityRules.isLocationOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @DeleteMapping("/{id}/banner/{bannerId}")
    public ResponseEntity<Location> deleteBanner(@PathVariable UUID id, @PathVariable UUID bannerId) {
        return ResponseEntity.status(HttpStatus.OK).body(locationService.deleteBanner(id, bannerId));
    }
}
