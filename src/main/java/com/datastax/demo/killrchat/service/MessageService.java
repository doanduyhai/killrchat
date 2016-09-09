package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.MessageEntity;
import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.utils.UUIDs;

import com.google.common.collect.Lists;
import info.archinnov.achilles.generated.manager.MessageEntity_Manager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;

import static com.datastax.demo.killrchat.entity.Schema.KILLRCHAT_USER;
import static java.lang.String.format;

@Service
public class MessageService {

    public static final String JOINING_MESSAGE = "%s joins the room";
    public static final String LEAVING_MESSAGE = "%s leaves the room";

    private MessageEntity_Manager manager;

    @Inject
    public MessageService(MessageEntity_Manager manager) {
        this.manager = manager;
    }

    public MessageModel postNewMessage(LightUserModel author, String roomName, String messageContent) {
        final MessageEntity entity = new MessageEntity(roomName, UUIDs.timeBased(), author, messageContent);
        manager.crud().insert(entity).executeAsync();
        return entity.toModel();
    }

    public List<MessageModel> fetchNextMessagesForRoom(String roomName, UUID fromMessageId, int pageSize) {

        return manager
                .dsl()
                .select()
                .allColumns_FromBaseTable()
                .where()
                .roomName().Eq(roomName)
                .messageId().Lt(fromMessageId)
                .limit(pageSize)
                .orderByMessageIdDescending()
                .getList()
                .stream()
                .map(MessageEntity::toModel)
                .collect(Collector.of(
                        ArrayList::new,
                        (List<MessageModel> l, MessageModel t) -> l.add(t),
                        (List<MessageModel> l, List<MessageModel> r) -> {
                            l.addAll(r);
                            return l;
                        },
                        Lists::<MessageModel>reverse));
    }

    public MessageModel createJoiningMessage(String roomName, LightUserModel participant) {
        final MessageEntity entity = new MessageEntity(roomName, UUIDs.timeBased(), KILLRCHAT_USER.toLightModel(), format(JOINING_MESSAGE, participant.getFormattedName()), true);
        manager.crud().insert(entity).executeAsync();
        return entity.toModel();
    }

    public MessageModel createLeavingMessage(String roomName, LightUserModel participant) {
        final MessageEntity entity = new MessageEntity(roomName, UUIDs.timeBased(), KILLRCHAT_USER.toLightModel(), format(LEAVING_MESSAGE, participant.getFormattedName()), true);
        manager.crud().insert(entity).executeAsync();
        return entity.toModel();
    }
}
