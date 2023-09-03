package ru.bogatov.quickmeet.repository.meet;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bogatov.quickmeet.entity.Banner;

import java.util.UUID;

public interface BannerRepository extends JpaRepository<Banner, UUID> {

}
