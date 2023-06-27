package ru.bogatov.quickmeet.repository.userdata;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bogatov.quickmeet.entity.City;

import java.util.UUID;
public interface CityRepository extends JpaRepository<City, UUID> {

}
