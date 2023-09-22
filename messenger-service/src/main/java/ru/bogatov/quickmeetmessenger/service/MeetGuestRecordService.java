package ru.bogatov.quickmeetmessenger.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bogatov.quickmeetmessenger.entity.MeetGuestRecord;
import ru.bogatov.quickmeetmessenger.repository.MeetGuestRecordRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static ru.bogatov.quickmeetmessenger.constant.CacheConstant.MEET_GUEST_CACHE;

@Service
@AllArgsConstructor
@Slf4j
public class MeetGuestRecordService {

    private final MeetGuestRecordRepository meetGuestRecordRepository;

    private final CacheManager cacheManager;

    /**
    * Создаем запись и удаляем на евенты с кор сервиса
    * Делаем метод получения гостей из встречи и кешируем его
    * Если кеша нету? либо удаляем его при перезапуске
    * То достаем из базы, кладем в модельку и созраняем, потом в паралель обновляем кеш и табличку
    * При получени сообщения от пользователя, отправляем всем гостям это сообщение
    * */
    public void create(UUID userId, UUID meetId) {
        MeetGuestRecord record = new MeetGuestRecord();
        record.setMeetId(meetId);
        record.setUserId(userId);
        meetGuestRecordRepository.save(record);
    }

    public void delete(UUID userId, UUID meetId) {
        meetGuestRecordRepository.deleteMeetGuestRecordByMeetIdAndUserId(meetId, userId);
    }

    @Cacheable(value = MEET_GUEST_CACHE, key = "#meetId")
    public Set<UUID> getMeetUsers(UUID meetId) {
        return new HashSet<>(meetGuestRecordRepository.findUsersByMeetId(meetId));
    }

    public void handleUserJoinEvent(UUID meetId, UUID userId) {
        log.info("handle join event meetId : {}, userId : {}", meetId, userId);
        create(userId, meetId);
        cacheManager.getCache(MEET_GUEST_CACHE).evict(meetId);
    }

    @Transactional
    public void handleUserLeftOrRemovedEvent(UUID meetId, UUID userId) {
        log.info("handle left event meetId : {}, userId : {}", meetId, userId);
        delete(userId, meetId);
        cacheManager.getCache(MEET_GUEST_CACHE).evict(meetId);
    }
}
