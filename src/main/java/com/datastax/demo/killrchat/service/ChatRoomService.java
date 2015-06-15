package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.ChatRoomEntity;
import com.datastax.demo.killrchat.entity.MessageEntity;
import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.exceptions.ChatRoomAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.ChatRoomDoesNotExistException;
import com.datastax.demo.killrchat.exceptions.IncorrectRoomException;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.security.repository.CassandraRepository;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.UDTValue;
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

import javax.annotation.Nullable;
import javax.inject.Inject;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOMS;
import static com.datastax.demo.killrchat.entity.Schema.CHATROOM_MESSAGES;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.driver.core.BatchStatement.Type.LOGGED;
import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static info.archinnov.achilles.type.OptionsBuilder.ifEqualCondition;
import static info.archinnov.achilles.type.OptionsBuilder.ifExists;
import static info.archinnov.achilles.type.OptionsBuilder.ifNotExists;
import static java.lang.String.format;

@Service
public class ChatRoomService {


    private static final String INCORRECT_CREATOR_FOR_DELETION = "You cannot delete this chat room because you're not the creator";
    private static final String INCORRECT_PARTICIPANTS_FOR_DELETION = "Chat room deletion failed, please reload the chat room and retry";

    public static final String DELETION_MESSAGE = "The room '%s' has been removed by '%s'";

    public final Function<UDTValue, LightUserModel> UDT_TO_LIGHT_USER_MODEL = new Function<UDTValue, LightUserModel>(){

        @Nullable
        @Override
        public LightUserModel apply(@Nullable UDTValue input) {
            return repository.userUdtMapper.fromUDT(input);
        }
    };

    private final Function<Row, ChatRoomModel> ROW_TO_CHAT_ROOM_MODEL = new Function<Row, ChatRoomModel>() {
        @Override
        public ChatRoomModel apply(Row row) {
            final Set<LightUserModel> participants = FluentIterable.from(row.getSet("participants", UDTValue.class))
                    .transform(UDT_TO_LIGHT_USER_MODEL)
                    .toSet();

            return new ChatRoomEntity(row.getString("room_name"),
                    repository.userUdtMapper.fromUDT(row.getUDTValue("creator")),
                    row.getDate("creation_date"),
                    row.getString("banner"),
                    participants)
                .toModel();
        }
    };

    @Inject
    Session session;

    @Inject
    CassandraRepository repository;


    public void createChatRoom(String roomName, String banner, LightUserModel creator) {
        final UDTValue udtCreator = repository.userUdtMapper.toUDT(creator);
        final Set<UDTValue> participantsList = Sets.newHashSet(udtCreator);
        final String creatorLogin = creator.getLogin();
        final BoundStatement bs = repository.createChatRoomPs.bind(roomName, udtCreator, creatorLogin, new Date(), banner, participantsList);

        final boolean applied = session.execute(bs).one().getBool("[applied]");
        if (!applied) {
            throw new ChatRoomAlreadyExistsException(format("The room '%s' already exists", roomName));
        }

        final BatchStatement batch = new BatchStatement(LOGGED);
        batch.add(repository.addChatRoomToUserPs.bind(Sets.newHashSet(roomName),creatorLogin));
        session.execute(batch);

    }

    public ChatRoomModel findRoomByName(String roomName) {
        final ChatRoomEntity chatRoom = repository.chatRoomMapper.get(roomName);
        if (chatRoom == null) {
            throw new ChatRoomDoesNotExistException(format("Chat room '%s' does not exists", roomName));
        }
        return chatRoom.toModel();
    }

    public List<ChatRoomModel> listChatRooms(int fetchSize) {

        final List<Row> foundChatRooms = session.execute(repository.listChatRoomPs.bind(fetchSize)).all();
        return FluentIterable.from(foundChatRooms).transform(ROW_TO_CHAT_ROOM_MODEL).toList();
    }

    public void addUserToRoom(String roomName, LightUserModel participant) {
        final String newParticipant = participant.getLogin();
        final UDTValue udtValue = repository.userUdtMapper.toUDT(participant);
        final BoundStatement bs = repository.addParticipantToChatRoomPs.bind(Sets.newHashSet(udtValue), roomName);

        final boolean applied = session.execute(bs).one().getBool("[applied]");

        if(!applied)  {
            throw new ChatRoomDoesNotExistException(format("The chat room '%s' does not exist", roomName));
        }

        // Add chat room to user chat room list too
        final BatchStatement batch = new BatchStatement(LOGGED);
        batch.add(repository.addChatRoomToUserPs.bind(Sets.newHashSet(roomName), newParticipant));
        session.execute(batch);
    }

    public void removeUserFromRoom(String roomName, LightUserModel participant) {
        final BatchStatement batch = new BatchStatement(LOGGED);
        final UDTValue udtValue = repository.userUdtMapper.toUDT(participant);
        final String participantToBeRemoved = participant.getLogin();
        batch.add(repository.removeParticipantFromChatRoomPs.bind(Sets.newHashSet(udtValue), roomName));

        // Remove chat room from user chat room list too
        batch.add(repository.removeChatRoomFromUserPs.bind(Sets.newHashSet(roomName), participantToBeRemoved));
        session.execute(batch);
    }

    public String deleteRoomWithParticipants(String creatorLogin, String roomName, Set<String> participants) {

        final Row one = session.execute(repository.chatRoomMapper.getQuery(roomName)).one();
        if (one == null) {
            throw new ChatRoomDoesNotExistException("Chat room '"+roomName+"' does not exists");
        }

        final BoundStatement bs = repository.deleteChatRoomPs.bind(roomName, creatorLogin, one.getSet("participants",UDTValue.class));
        final Row deleted = session.execute(bs).one();
        final boolean applied = deleted.getBool("[applied]");
        if (!applied) {
            final String creator = deleted.getString("creator_login");
            String message = creator.equals(creatorLogin) ? INCORRECT_PARTICIPANTS_FOR_DELETION:INCORRECT_CREATOR_FOR_DELETION;
            throw new IncorrectRoomException(message);
        }

        // Delete all chat messages from room
        final BatchStatement batch = new BatchStatement(LOGGED);
        batch.add(repository.deleteAllMessagePs.bind(roomName));


        // Remove this chat room from the chat room list of ALL current participants using BATCH for automatic retry
        for (String participantLogin : participants) {
            batch.add(repository.removeChatRoomFromUserPs.bind(Sets.newHashSet(roomName), participantLogin));
        }

        session.execute(batch);

        return String.format(DELETION_MESSAGE, roomName, creatorLogin);
    }

}
