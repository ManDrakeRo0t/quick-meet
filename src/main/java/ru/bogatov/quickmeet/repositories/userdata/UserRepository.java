package ru.bogatov.quickmeet.repositories.userdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bogatov.quickmeet.entities.User;
import ru.bogatov.quickmeet.entities.auth.UserForAuth;

import java.util.Optional;
import java.util.UUID;
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);
    @Query(value = "select cast(u.id as varchar) as id, u.account_class, u.phone_number, u.refresh,u.is_active, u.is_blocked " +
            "from usr u " +
            "where u.phone_number = ?1", nativeQuery = true)
    Optional<UserForAuth> findByPhoneNumberForAuth(String phoneNumber);
    @Modifying
    @Query(value = "update usr set refresh = :refresh_token where id = :id", nativeQuery = true)
    void updateRefreshToken(@Param("id") UUID id, @Param("refresh_token") String refresh);

}
