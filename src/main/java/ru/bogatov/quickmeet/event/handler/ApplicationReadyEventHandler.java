package ru.bogatov.quickmeet.event.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import ru.bogatov.quickmeet.service.meet.MeetCategoryService;
import ru.bogatov.quickmeet.service.meet.MeetService;

import static ru.bogatov.quickmeet.constant.CacheConstants.CACHES_NAMES;

@Component
@Slf4j
public class ApplicationReadyEventHandler implements ApplicationListener<ApplicationReadyEvent> {
    private final CacheManager cacheManager;
    private final MeetService meetService;
    private final MeetCategoryService meetCategoryService;
    public ApplicationReadyEventHandler(CacheManager cacheManager, MeetService meetService, MeetCategoryService meetCategoryService) {
        this.cacheManager = cacheManager;
        this.meetService = meetService;
        this.meetCategoryService = meetCategoryService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        CACHES_NAMES.forEach(name -> {
            log.info("Cleaning cache : {}" , name);
            Cache cacheForName = cacheManager.getCache(name);
            if (cacheForName != null) {
                cacheForName.clear();
            }
        });

        if (meetCategoryService.findAll().isEmpty()) {
            log.info("Categories are empty, create all");
            meetCategoryService.initCategories();
        }

    }
}
