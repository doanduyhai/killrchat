package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.ChatRoomEntity;
import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.exceptions.ChatRoomAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.ChatRoomDoesNotExistException;
import com.datastax.demo.killrchat.exceptions.IncorrectRoomException;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;

import com.datastax.driver.core.querybuilder.Delete;
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
import static com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static java.lang.String.format;


@Service
public class ChatRoomService {

    private static final Select SELECT_ROOMS = select().from(KEYSPACE, CHATROOMS).limit(bindMarker("fetchSize"));
    private static final Delete.Where DELETE_ROOM_MESSAGES = delete().from(KEYSPACE, CHATROOM_MESSAGES).where(eq("room_name", bindMarker("roomName")));

    private static final Function<ChatRoomEntity, ChatRoomModel> CHAT_ROOM_TO_MODEL = new Function<ChatRoomEntity, ChatRoomModel>() {
        @Override
        public ChatRoomModel apply(ChatRoomEntity entity) {
            return entity.toModel();
        }
    };

    public static final String DELETION_MESSAGE = "The room '%s' has been removed by '%s'";

    @Inject
    PersistenceManager manager;

    public void createChatRoom(String roomName, String banner, LightUserModel creator) {
        final Set<LightUserModel> participantsList = Sets.newHashSet(creator);

        final String creatorLogin = creator.getLogin();
        final ChatRoomEntity room = new ChatRoomEntity(roomName, creator, new Date(), banner, participantsList);
        try {
            manager.insert(room, OptionsBuilder.ifNotExists());
        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomAlreadyExistsException(format("The room '%s' already exists", roomName));
        }

        final UserEntity userProxy = manager.forUpdate(UserEntity.class, creatorLogin);
        userProxy.getChatRooms().add(roomName);
        manager.update(userProxy);
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

        /**
         * Specs
         *
         *
         *  - when a participant is joining a chat room, we must add this participant to ChatRoomEntity.participants.
         *    Use the Achilles update by proxy API as in the createChatRoom() method above.
         *
         *  - in order to be concurrency-proof, we should add a new participant only if the room does exist.
         *    It can be achieved by an UPDATE with a condition on the room name (property 'name') using
         *    LightWeight Transaction. Ex:
         *
         *
         *      final ChatRoomEntity chatRoomProxy = manager.forUpdate(ChatRoomEntity.class, primary_key_of_a_chat_room);
         *
         *      ??? Implement code here
         *
         *      manager.update(proxy, OptionsBuilder.ifExists());
         *
         *  - for documentation on LightWeight Transaction API (optional): https://github.com/doanduyhai/Achilles/wiki/Lightweight-Transaction
         */

        //Implement the service here

        try {

            //Implement the service here

        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomDoesNotExistException(format("The chat room '%s' does not exist", roomName));
        }

        // Add chat room to user chat room list too
        final UserEntity userProxy = manager.forUpdate(UserEntity.class, newParticipant);
        userProxy.getChatRooms().add(roomName);
        manager.update(userProxy);
    }

    public void removeUserFromRoom(String roomName, LightUserModel participant) {
        final String participantToBeRemoved = participant.getLogin();

        /**
         * Specs
         *
         *
         *  - when a participant is leaving a chat room, we must remove this participant from ChatRoomEntity.participants.
         *    Use the Achilles update by proxy API as in the createChatRoom() method above.
         *
         *  - this time we DO NOT NEED to ensure that the chat room still exists. Deleting = creating a tombstone
         *    in Cassandra so that removing a participant from a non-existing room is idempotent and has no side-effect
         *
         *    We could have used LightWeight Transaction for participant removal too but it is un-necessary
         *
         *      final ChatRoomEntity chatRoomProxy = manager.forUpdate(ChatRoomEntity.class, primary_key_of_a_chat_room);
         *
         *      ??? Implement code here
         *
         *      manager.update(proxy);
         *
         */


        //Implement the service here


        // Remove chat room from user chat room list too
        final UserEntity userProxy = manager.forUpdate(UserEntity.class, participantToBeRemoved);
        userProxy.getChatRooms().remove(roomName);
        manager.update(userProxy);
    }

    public String deleteRoomWithParticipants(String creatorLogin, String roomName, Set<String> participants) {

        /**
         * Specs part 1
         *
         *  - we must delete a room if and only if the current user login (parameter creatorLogin) does match
         *    the 'creator_login' column in the chat_rooms table. Only the creator of a room can delete it.
         *    For this we'll rely again on LightWeight Transaction
         *
         *  - to avoid the read-before-write ANTI-PATTERN (manager.find() followed by manager.delete()), use:
         *
         *      manager.deleteById(MyEntity.class, primaryKey, OptionsBuilder.ifEqualCondition(???,???))
         *
         */

        try {

            //Implement the service here

        } catch (AchillesLightWeightTransactionException ex) {
            throw new IncorrectRoomException(ex.getMessage());
        }


        /**
         * Specs part 2
         *
         * Delete all chat messages from room
         *
         *  - use the manager.nativeQuery(DELETE_ROOM_MESSAGES, boundValues).execute()
         *
         *  - the DELETE_ROOM_MESSAGES statement is already given above, just re-use it
         */


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