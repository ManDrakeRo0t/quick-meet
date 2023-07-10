package ru.bogatov.quickmeet.service.meet;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entity.MeetCategory;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.repository.meet.MeetCategoryRepository;

import java.util.List;
import java.util.UUID;

import static ru.bogatov.quickmeet.constant.CacheConstants.MEET_CATEGORY_CACHE;

@Service
public class MeetCategoryService {

    private final MeetCategoryRepository meetCategoryRepository;
    private final CacheManager cacheManager;

    public MeetCategoryService(MeetCategoryRepository meetCategoryRepository, CacheManager cacheManager) {
        this.meetCategoryRepository = meetCategoryRepository;
        this.cacheManager = cacheManager;
    }

    public MeetCategory createCategory(MeetCategory body) {
        body.setHidden(false);
        MeetCategory meetCategory = meetCategoryRepository.save(body);
        cacheManager.getCache(MEET_CATEGORY_CACHE).put(meetCategory.getId(), meetCategory);
        cacheManager.getCache(MEET_CATEGORY_CACHE).evict("false");
        return meetCategory;
    }
    @Cacheable(value = MEET_CATEGORY_CACHE, key = "#returnHidden")
    public List<MeetCategory> findAllByHidden(boolean returnHidden) {
        if (returnHidden) {
            return meetCategoryRepository.findAll();
        }
        return meetCategoryRepository.findAllByHiddenFalse();
    }
    @Cacheable(value = MEET_CATEGORY_CACHE, key = "#id")
    public MeetCategory findById(UUID id) {
        return meetCategoryRepository.findById(id).orElseThrow(() -> ErrorUtils.buildException(ApplicationError.DATA_NOT_FOUND_ERROR, "Meet category not found"));
    }

    public MeetCategory updateCategory(UUID id, MeetCategory body) {
        MeetCategory source = findById(id);
        source.setHidden(body.isHidden());
        source.setName(body.getName());
        MeetCategory meetCategory = meetCategoryRepository.save(source);
        cacheManager.getCache(MEET_CATEGORY_CACHE).put(id, meetCategory);
        cacheManager.getCache(MEET_CATEGORY_CACHE).evict("true");
        cacheManager.getCache(MEET_CATEGORY_CACHE).evict("false");
        return meetCategory;
    }
}
