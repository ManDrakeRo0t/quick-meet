package ru.bogatov.quickmeet.repository.userdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bogatov.quickmeet.entity.Guest;

import java.util.UUID;

public interface GuestRepository extends JpaRepository<Guest, UUID> {

    @Modifying
    @Query(nativeQuery = true, value = "update guest set is_attend = :isAttend where id = :id")
    void setAttend(@Param("isAttend") boolean isAttend, @Param("id") UUID id);

}
