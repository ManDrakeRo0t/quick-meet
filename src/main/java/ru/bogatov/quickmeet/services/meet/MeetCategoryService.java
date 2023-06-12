package ru.bogatov.quickmeet.services.meet;

import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entities.MeetCategory;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.repositories.meet.MeetCategoryRepository;

import java.util.List;
import java.util.UUID;

@Service
public class MeetCategoryService {

    MeetCategoryRepository meetCategoryRepository;

    public MeetCategoryService(MeetCategoryRepository meetCategoryRepository) {
        this.meetCategoryRepository = meetCategoryRepository;
    }

    public MeetCategory createCategory(MeetCategory body) {
        body.setHidden(false);
        return meetCategoryRepository.save(body);
    }

    public List<MeetCategory> findAllByHidden(boolean returnHidden) {
        if (returnHidden) {
            return meetCategoryRepository.findAll();
        }
        return meetCategoryRepository.findAllByHiddenFalse();
    }

    public MeetCategory findById(UUID id) {
        return meetCategoryRepository.findById(id).orElseThrow(() -> ErrorUtils.buildException(ApplicationError.DATA_NOT_FOUND_ERROR, "Meet category not found"));
    }

    public MeetCategory updateCategory(UUID id, MeetCategory body) {
        MeetCategory source = findById(id);
        source.setHidden(body.isHidden());
        source.setName(body.getName());
        return meetCategoryRepository.save(source);
    }
}
