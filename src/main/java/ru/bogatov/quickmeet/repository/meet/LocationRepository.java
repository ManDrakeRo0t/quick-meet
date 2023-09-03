package ru.bogatov.quickmeet.repository.meet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.bogatov.quickmeet.entity.Location;

import java.util.Set;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    @Query(nativeQuery = true, value = "select cast(id as varchar) as id from location where user_id = :userId")
    Set<UUID> findAllByUserId(UUID userId);

}
