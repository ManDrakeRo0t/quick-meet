package ru.bogatov.quickmeetmessenger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.bogatov.quickmeetmessenger.entity.Message;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query(nativeQuery = true, value = "select * from message where destination_id = :chatId order by send_time desc limit :limit offset :offset")
    Set<Message> defaultSearch(UUID chatId, int limit, int offset);

    @Query(nativeQuery = true, value = "select * from(select * from message where destination_id = :chatId " +
            "and send_time > :readTime union all (select * from message where destination_id = :chatId and send_time < :readTime order by send_time desc limit :limit )) as m order by send_time desc")
    Set<Message> historySearch(UUID chatId, int limit, LocalDateTime readTime);

}
