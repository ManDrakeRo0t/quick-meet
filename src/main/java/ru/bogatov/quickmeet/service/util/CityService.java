package ru.bogatov.quickmeet.service.util;

import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entity.City;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.repository.userdata.CityRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public City createOrGetExisting(City source) {
        if (this.cityRepository.existsById(source.getId())) {
            return source;
        } else {
            return this.cityRepository.save(source);
        }
    }

    public City getById(UUID id) {
        return cityRepository.findById(id).orElseThrow( () -> ErrorUtils.buildException(ApplicationError.DATA_NOT_FOUND_ERROR, "City not found"));
    }

    public City createOrGetExisting(UUID id, String name) {
        City city = new City();
        city.setId(id);
        city.setName(name);
        Optional<City> existing = this.cityRepository.findById(id);
        return existing.orElseGet(() -> this.cityRepository.save(city));
    }
}
