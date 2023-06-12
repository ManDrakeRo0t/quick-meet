package ru.bogatov.quickmeet.repositories.meet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.bogatov.quickmeet.entities.MeetCategory;

import java.util.List;
import java.util.UUID;

public interface MeetCategoryRepository extends JpaRepository<MeetCategory, UUID> {

    @Query(value = "select * from meet_category where is_hidden = false", nativeQuery = true)
    List<MeetCategory> findAllByHiddenFalse();

    //List<MeetCategory> findBy();


}
