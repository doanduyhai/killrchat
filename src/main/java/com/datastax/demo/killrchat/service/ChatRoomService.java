package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.ChatRoomEntity;
import com.datastax.demo.killrchat.entity.MessageEntity;
import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.exceptions.ChatRoomAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.ChatRoomDoesNotExistException;
import com.datastax.demo.killrchat.exceptions.IncorrectRoomException;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.Batch;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOMS;
import static com.datastax.demo.killrchat.entity.Schema.CHATROOM_MESSAGES;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static info.archinnov.achilles.type.OptionsBuilder.ifEqualCondition;
import static info.archinnov.achilles.type.OptionsBuilder.ifNotExists;
import static java.lang.String.format;

@Service
public class ChatRoomService {

    private static final Select SELECT_ROOMS = select().from(KEYSPACE, CHATROOMS).limit(bindMarker("fetchSize"));
    private static final Delete.Where DELETE_ROOM_MESSAGES = delete().from(KEYSPACE, CHATROOM_MESSAGES).where(QueryBuilder.eq("room_name", bindMarker("roomName")));

    public static final String DELETION_MESSAGE = "The room '%s' has been removed by '%s'";

    private static final Function<ChatRoomEntity, ChatRoomModel> CHAT_ROOM_TO_MODEL = new Function<ChatRoomEntity, ChatRoomModel>() {
        @Override
        public ChatRoomModel apply(ChatRoomEntity entity) {
            return entity.toModel();
        }
    };

    @Inject
    PersistenceManager manager;


    public void createChatRoom(String roomName, String banner, LightUserModel creator) {
        final Set<LightUserModel> participantsList = Sets.newHashSet(creator);
        final String creatorLogin = creator.getLogin();
        final ChatRoomEntity room = new ChatRoomEntity(roomName, creator, new Date(), banner, participantsList);
        try {
            manager.insert(room, ifNotExists());
        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomAlreadyExistsException(format("The room '%s' already exists", roomName));
        }

        final Batch batch = manager.createBatch();
        final UserEntity userProxy = manager.forUpdate(UserEntity.class, creatorLogin);
        userProxy.getChatRooms().add(roomName);
        batch.update(userProxy);
        batch.endBatch();

    }

    public ChatRoomModel findRoomByName(String roomName) {
        final ChatRoomEntity chatRoom = manager.find(ChatRoomEntity.class, roomName);
        if (chatRoom == null) {
            throw new ChatRoomDoesNotExistException(format("Chat room '%s' does not exists", roomName));
        }
        return chatRoom.toModel();
    }

    public List<ChatRoomModel> listChatRooms(int fetchSize) {
        final List<ChatRoomEntity> foundChatRooms = manager.typedQuery(ChatRoomEntity.class, SELECT_ROOMS, new Object[]{fetchSize}).get();
        return FluentIterable.from(foundChatRooms).transform(CHAT_ROOM_TO_MODEL).toList();
    }

    public void addUserToRoom(String roomName, LightUserModel participant) {
        final String newParticipant = participant.getLogin();

        try {
            final ChatRoomEntity chatRoomProxy = manager.forUpdate(ChatRoomEntity.class, roomName);
            chatRoomProxy.getParticipants().add(participant);
            manager.update(chatRoomProxy, ifEqualCondition("name", roomName));
        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomDoesNotExistException(format("The chat room '%s' does not exist", roomName));
        }

        // Add chat room to user chat room list too
        final Batch batch = manager.createBatch();
        final UserEntity userProxy = manager.forUpdate(UserEntity.class, newParticipant);
        userProxy.getChatRooms().add(roomName);
        batch.update(userProxy);
        batch.endBatch();
    }

    public void removeUserFromRoom(String roomName, LightUserModel participant) {
        final Batch batch = manager.createBatch();

        final String participantToBeRemoved = participant.getLogin();
        final ChatRoomEntity chatRoomProxy = manager.forUpdate(ChatRoomEntity.class, roomName);
        chatRoomProxy.getParticipants().remove(participant);
        batch.update(chatRoomProxy);

        // Remove chat room from user chat room list too
        final UserEntity userProxy = manager.forUpdate(UserEntity.class, participantToBeRemoved);
        userProxy.getChatRooms().remove(roomName);
        batch.update(userProxy);

        batch.endBatch();
    }

    public String deleteRoomWithParticipants(String creatorLogin, String roomName, Set<String> participants) {
        try {
            manager.deleteById(ChatRoomEntity.class, roomName, ifEqualCondition("creator_login", creatorLogin));
        } catch (AchillesLightWeightTransactionException ex) {
            throw new IncorrectRoomException(ex.getMessage());
        }

        // Delete all chat messages from room
        final Batch chatMessageBatch = manager.createBatch();
        chatMessageBatch.batchNativeStatement(DELETE_ROOM_MESSAGES, roomName);
        chatMessageBatch.endBatch();

        // Remove this chat room from the chat room list of ALL current participants using BATCH for automatic retry
        final Batch batch = manager.createBatch();

        for (String participantLogin : participants) {
            final UserEntity proxy = manager.forUpdate(UserEntity.class, participantLogin);
            proxy.getChatRooms().remove(roomName);
            batch.update(proxy);
        }

        //Flush all the mutations here
        batch.endBatch();

        return String.format(DELETION_MESSAGE, roomName, creatorLogin);
    }

}
