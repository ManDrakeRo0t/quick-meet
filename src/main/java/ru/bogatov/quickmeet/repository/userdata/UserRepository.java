package ru.bogatov.quickmeet.repository.userdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.entity.auth.UserForAuth;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);
    @Query(nativeQuery = true, value = "select cast(id as varchar) as id from usr where phone_number = :phoneNumber")
    Optional<UUID> isUserExistWithPhoneNumber(@Param(value = "phoneNumber") String phoneNumber);
    @Query(nativeQuery = true, value = "select cast(id as varchar) as id from usr where email = :mail")
    Optional<UUID> isUserExistWithMail(@Param(value = "mail") String mail);
    @Query(value = "select cast(u.id as varchar) as id, u.account_class, u.phone_number, u.refresh,u.is_active, u.password, u.is_blocked, u.role " +
            "from usr u " +
            "where u.id = ?1", nativeQuery = true)
    Optional<UserForAuth> findByPhoneNumberForAuth(UUID userId);
    @Modifying
    @Query(value = "update usr set refresh = :refresh_token where id = :id", nativeQuery = true)
    void updateRefreshToken(@Param("id") UUID id, @Param("refresh_token") String refresh);

    @Modifying
    @Query(value = "update usr set account_rank = :rank, rank_update_date = :lastUpdate where id = :id", nativeQuery = true)
    void updateRankInfo(@Param("id") UUID id, @Param("rank") float rank, @Param("lastUpdate") Date date);

    @Modifying
    @Query(value = "update usr set is_email_confirmed = :isConfirmed where email = :mail", nativeQuery = true)
    void setMailConfirmation(@Param("mail") String mail, @Param("isConfirmed") boolean isConfirmed);

    @Query(nativeQuery = true, value = "select cast(guest.meet_id as varchar) from guest where user_id = :userId union all select cast(meet.id as varchar) from meet where user_id = :userId")
    Set<UUID> getAllApplicableChats(@Param("userId") UUID userId);

}
