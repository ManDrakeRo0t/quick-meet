package ru.bogatov.quickmeet.repository.file;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bogatov.quickmeet.entity.File;

import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
}
