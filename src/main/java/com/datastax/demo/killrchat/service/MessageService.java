package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.MessageEntity;
import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.security.repository.CassandraRepository;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.List;
import java.util.UUID;

import static com.datastax.demo.killrchat.entity.Schema.KILLRCHAT_USER;
import static java.lang.String.format;

@Service
public class MessageService {

    private final Function<Row, MessageModel> TO_MODEL = new Function<Row, MessageModel>() {
        @Override
        public MessageModel apply(Row row) {
            return new MessageEntity(
                    row.getString("room_name"),
                    row.getUUID("message_id"),
                    repository.userUdtMapper.fromUDT(row.getUDTValue("author")),
                    row.getString("content"))
                .toModel();
        }
    };

    public static final String JOINING_MESSAGE = "%s joins the room";
    public static final String LEAVING_MESSAGE = "%s leaves the room";

    @Inject
    Session session;

    @Inject
    CassandraRepository repository;

    public MessageModel postNewMessage(LightUserModel author, String roomName, String messageContent) {
        final MessageEntity entity = new MessageEntity(roomName, UUIDs.timeBased(), author, messageContent);
        repository.messageMapper.save(entity);
        return entity.toModel();
    }

    public List<MessageModel> fetchNextMessagesForRoom(String roomName, UUID fromMessageId, int pageSize) {
        final BoundStatement bs = repository.selectMessagePs.bind(roomName, fromMessageId, pageSize);
        final List<Row> messages = session.execute(bs).all();

        return FluentIterable.from(messages).transform(TO_MODEL).toList().reverse();
    }

    public MessageModel createJoiningMessage(String roomName, LightUserModel participant) {
        final MessageEntity entity = new MessageEntity(roomName, UUIDs.timeBased(), KILLRCHAT_USER.toLightModel(), format(JOINING_MESSAGE, participant.getFormattedName()), true);
        repository.messageMapper.save(entity);
        return entity.toModel();
    }

    public MessageModel createLeavingMessage(String roomName, LightUserModel participant) {
        final MessageEntity entity = new MessageEntity(roomName, UUIDs.timeBased(), KILLRCHAT_USER.toLightModel(), format(LEAVING_MESSAGE, participant.getFormattedName()), true);
        repository.messageMapper.save(entity);
        return entity.toModel();
    }
}
