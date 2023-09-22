package ru.bogatov.quickmeetmessenger.event.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import static ru.bogatov.quickmeetmessenger.constant.CacheConstant.CACHE_NAMES;


@Component
@AllArgsConstructor
@Slf4j
public class ApplicationReadyEventHandler implements ApplicationListener<ApplicationReadyEvent> {

    private final CacheManager cacheManager;
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        CACHE_NAMES.forEach(cacheName -> {
            log.info("Cleaning cache : {}" , cacheName);
            Cache cacheForName = cacheManager.getCache(cacheName);
            if (cacheForName != null) {
                cacheForName.clear();
            }
        });
    }
}
