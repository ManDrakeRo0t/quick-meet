package ru.bogatov.quickmeet.repository.meet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.bogatov.quickmeet.entity.Location;

import java.util.Set;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    @Query(nativeQuery = true, value = "select cast(id as varchar) as id from location where user_id = :userId and is_hidden = false")
    Set<UUID> findAllByUserId(UUID userId);

    @Query(nativeQuery = true, value = "select cast(id as varchar) as id from location where latitude between ?1 and ?2 and longevity between ?3 and ?4 and is_hidden = false")
    Set<UUID> search(double lat_min, double lat_max, double lon_min, double lon_max);

}
