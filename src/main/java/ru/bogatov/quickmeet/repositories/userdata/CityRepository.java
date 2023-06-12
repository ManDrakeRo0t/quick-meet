package ru.bogatov.quickmeet.repositories.userdata;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bogatov.quickmeet.entities.City;

import java.util.UUID;
public interface CityRepository extends JpaRepository<City, UUID> {

}
