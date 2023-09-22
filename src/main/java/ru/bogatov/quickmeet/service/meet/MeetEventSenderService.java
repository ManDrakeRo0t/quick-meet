package ru.bogatov.quickmeet.service.meet;

import io.netty.util.internal.StringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.event.rabbitmq.MeetUpdateEvent;
import ru.bogatov.quickmeet.model.enums.MeetStatus;
import ru.bogatov.quickmeet.model.enums.MeetUpdateEventType;
import ru.bogatov.quickmeet.model.request.MeetUpdateBody;

import java.util.UUID;

import static ru.bogatov.quickmeet.config.rabbitmq.RabbitMqConfig.MEET_EXCHANGE;

@Service
public class MeetEventSenderService {
    private final RabbitTemplate rabbitTemplate;

    public MeetEventSenderService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendUsedJoinedEvent(UUID meetId, UUID userId) {
        MeetUpdateEvent event = MeetUpdateEvent.builder()
                .type(MeetUpdateEventType.USER_JOINED)
                .field("")
                .newValue("")
                .isSystemUpdate(false)
                .userId(userId)
                .meetId(meetId)
                .build();
        sendEvent(event);
    }

    public void sendUsedLeftEvent(UUID meetId, UUID userId) {
        MeetUpdateEvent event = MeetUpdateEvent.builder()
                .type(MeetUpdateEventType.USER_LEFT)
                .field("")
                .newValue("")
                .isSystemUpdate(false)
                .userId(userId)
                .meetId(meetId)
                .build();
        sendEvent(event);
    }

    public void sendUsedRemovedEvent(UUID meetId, UUID userId) {
        MeetUpdateEvent event = MeetUpdateEvent.builder()
                .type(MeetUpdateEventType.USER_DELETED)
                .field("")
                .newValue("")
                .isSystemUpdate(false)
                .userId(userId)
                .meetId(meetId)
                .build();
        sendEvent(event);
    }

    public void sendMeetCreatedEvent(UUID meetId, UUID userId) {
        MeetUpdateEvent event = MeetUpdateEvent.builder()
                .type(MeetUpdateEventType.MEET_CREATED)
                .field("")
                .newValue("")
                .isSystemUpdate(false)
                .userId(userId)
                .meetId(meetId)
                .build();
        sendEvent(event);
    }

    public int sendMeetUpdatedEvent(UUID meetId, MeetUpdateBody meet, String categoryName, int oldDuration) {
        int updatedFieldsCount = 0;
        MeetUpdateEvent.MeetUpdateEventBuilder builder = MeetUpdateEvent.builder()
                .meetId(meetId)
                .userId(null)
                .isSystemUpdate(false)
                .type(MeetUpdateEventType.MEET_UPDATED);
        if (!StringUtil.isNullOrEmpty(meet.getName())) {
            MeetUpdateEvent event = builder
                    .field("name")
                    .newValue(meet.getName()).build();
            updatedFieldsCount++;
            sendEvent(event);
        }
        if (!StringUtil.isNullOrEmpty(meet.getDescription())) {
            MeetUpdateEvent event = builder
                    .field("description")
                    .newValue(meet.getDescription()).build();
            updatedFieldsCount++;
            sendEvent(event);
        }
        if (meet.getTime() != null) {
            MeetUpdateEvent event = builder
                    .field("dateTime")
                    .newValue(meet.getTime().toString()).build();
            updatedFieldsCount++;
            sendEvent(event);
        }
        if (meet.getExpectedDuration() != null && meet.getExpectedDuration() != oldDuration) {
            MeetUpdateEvent event = builder
                    .field("expectedDuration")
                    .newValue(String.valueOf(meet.getExpectedDuration())).build();
            updatedFieldsCount++;
            sendEvent(event);
        }
        if (meet.getCategoryId() != null) {
            MeetUpdateEvent event = builder
                    .field("category")
                    .newValue(categoryName).build();
            updatedFieldsCount++;
            sendEvent(event);
        }
        return updatedFieldsCount;
    }

    public void sendMeetUpdatedStateEvent(MeetStatus newValue, UUID meetId, boolean isSystemUpdate) {
        MeetUpdateEvent event = MeetUpdateEvent.builder()
                .type(MeetUpdateEventType.MEET_UPDATED)
                .field("meetStatus")
                .newValue(newValue.getValue())
                .isSystemUpdate(isSystemUpdate)
                .userId(null)
                .meetId(meetId)
                .build();
        sendEvent(event);
    }

    public void sendMeetAvatarUpdatedEvent(UUID meetId) {
        MeetUpdateEvent event = MeetUpdateEvent.builder()
                .type(MeetUpdateEventType.MEET_UPDATED)
                .field("avatar")
                .newValue("")
                .isSystemUpdate(false)
                .userId(null)
                .meetId(meetId)
                .build();
        sendEvent(event);
    }

    public void sendEvent(MeetUpdateEvent event) {
        rabbitTemplate.convertAndSend(MEET_EXCHANGE, "meet.event", event);
    }
}
