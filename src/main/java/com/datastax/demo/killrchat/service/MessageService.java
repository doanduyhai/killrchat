package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.MessageEntity;
import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static com.datastax.demo.killrchat.entity.Schema.KILLRCHAT_USER;
import static java.lang.String.format;

@Service
public class MessageService {

    private static final Function<MessageEntity, MessageModel> TO_MODEL = new Function<MessageEntity, MessageModel>() {
        @Override
        public MessageModel apply(MessageEntity entity) {
            return entity.toModel();
        }
    };

    public static final String JOINING_MESSAGE = "%s joins the room";
    public static final String LEAVING_MESSAGE = "%s leaves the room";

    @Inject
    PersistenceManager manager;

    public MessageModel postNewMessage(LightUserModel author, String roomName, String messageContent) {
        final MessageEntity entity = new MessageEntity(roomName, UUIDs.timeBased(), author, messageContent);
        manager.insert(entity);
        return entity.toModel();
    }

    public List<MessageModel> fetchNextMessagesForRoom(String roomName, UUID fromMessageId, int pageSize) {
        final List<MessageEntity> messages = manager.sliceQuery(MessageEntity.class)
                .forSelect()
                .withPartitionComponents(roomName)
                .fromClusterings(fromMessageId)
                .fromExclusiveToInclusiveBounds()
                .get(pageSize);

        return FluentIterable.from(messages).transform(TO_MODEL).toList().reverse();
    }

    public MessageModel createJoiningMessage(String roomName, LightUserModel participant) {
        final MessageEntity entity = new MessageEntity(roomName, UUIDs.timeBased(), KILLRCHAT_USER.toLightModel(), format(JOINING_MESSAGE, participant.getFormattedName()), true);
        manager.insert(entity);
        return entity.toModel();
    }

    public MessageModel createLeavingMessage(String roomName, LightUserModel participant) {
        final MessageEntity entity = new MessageEntity(roomName, UUIDs.timeBased(), KILLRCHAT_USER.toLightModel(), format(LEAVING_MESSAGE, participant.getFormattedName()), true);
        manager.insert(entity);
        return entity.toModel();
    }
}
