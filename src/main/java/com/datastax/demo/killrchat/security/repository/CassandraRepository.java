package com.datastax.demo.killrchat.security.repository;

import com.datastax.demo.killrchat.entity.ChatRoomEntity;
import com.datastax.demo.killrchat.entity.MessageEntity;
import com.datastax.demo.killrchat.entity.PersistentTokenEntity;
import com.datastax.demo.killrchat.entity.Schema;
import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.security.authority.UserAuthority;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.UDTMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOMS;
import static com.datastax.demo.killrchat.entity.Schema.CHATROOM_MESSAGES;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.PERSISTENT_TOKEN;
import static com.datastax.demo.killrchat.entity.Schema.USERS;
import static com.datastax.driver.core.querybuilder.QueryBuilder.add;
import static com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.lt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.remove;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.core.querybuilder.QueryBuilder.ttl;
import static com.datastax.driver.core.querybuilder.QueryBuilder.update;

@Repository
public class CassandraRepository {

    @Inject
    public Session session;

    public Mapper<UserEntity> userMapper;
    public UDTMapper<LightUserModel> userUdtMapper;
    public UDTMapper<UserAuthority> userAuthorityUdtMapper;
    public Mapper<ChatRoomEntity> chatRoomMapper;
    public Mapper<MessageEntity> messageMapper;
    public Mapper<PersistentTokenEntity> persistentTokenMapper;

    public PreparedStatement createUserPs;
    public PreparedStatement addChatRoomToUserPs;
    public PreparedStatement removeChatRoomFromUserPs;

    public PreparedStatement createChatRoomPs;
    public PreparedStatement listChatRoomPs;
    public PreparedStatement addParticipantToChatRoomPs;
    public PreparedStatement removeParticipantFromChatRoomPs;
    public PreparedStatement deleteChatRoomPs;


    public PreparedStatement selectMessagePs;
    public PreparedStatement deleteAllMessagePs;

    public PreparedStatement createTokenPs;
    public PreparedStatement updateTokenPs;

    @PostConstruct
    public void generateMappersAndPreparedStatements() {
        final MappingManager mappingManager = new MappingManager(session);
        userMapper = mappingManager.mapper(UserEntity.class);
        userUdtMapper = mappingManager.udtMapper(LightUserModel.class);
        userAuthorityUdtMapper = mappingManager.udtMapper(UserAuthority.class);
        chatRoomMapper = mappingManager.mapper(ChatRoomEntity.class);
        messageMapper = mappingManager.mapper(MessageEntity.class);
        persistentTokenMapper = mappingManager.mapper(PersistentTokenEntity.class);

        // Create User
        RegularStatement createUser = insertInto(KEYSPACE, USERS)
                .ifNotExists()
                .value("login", bindMarker("login"))
                .value("pass", bindMarker("pass"))
                .value("firstname", bindMarker("firstname"))
                .value("lastname", bindMarker("lastname"))
                .value("email", bindMarker("email"))
                .value("bio", bindMarker("bio"));
        createUserPs = session.prepare(createUser);

        // Add chat room to user
        RegularStatement addChatRoomToUser = update(KEYSPACE, USERS)
                .with(add("chat_rooms", bindMarker("chat_room")))
                .where(eq("login", bindMarker("login")));
        addChatRoomToUserPs = session.prepare(addChatRoomToUser);

        // Remove chat room from user
        RegularStatement removeChatRoomFromUser = update(KEYSPACE, USERS)
                .with(remove("chat_rooms", bindMarker("chat_room")))
                .where(eq("login", bindMarker("login")));
        removeChatRoomFromUserPs = session.prepare(removeChatRoomFromUser);

        // Create chat room
        RegularStatement createChatRoom = insertInto(KEYSPACE, CHATROOMS)
                .ifNotExists()
                .value("room_name", bindMarker("room_name"))
                .value("creator", bindMarker("creator"))
                .value("creator_login", bindMarker("creator_login"))
                .value("creation_date", bindMarker("creation_date"))
                .value("banner", bindMarker("banner"))
                .value("participants", bindMarker("participants"));
        createChatRoomPs = session.prepare(createChatRoom);

        // List chat rooms
        RegularStatement listChatRoom = select().from(KEYSPACE, CHATROOMS)
                .limit(bindMarker("fetchSize"));
        listChatRoomPs = session.prepare(listChatRoom);

        // Add participant to chat room
        RegularStatement addParticipantToChatRoom =
                new SimpleStatement("UPDATE "+KEYSPACE+"."+CHATROOMS+" SET participants = participants + :participant WHERE room_name = :room_name IF EXISTS");
        addParticipantToChatRoomPs = session.prepare(addParticipantToChatRoom);

        // Remove participant from chat room
        RegularStatement removeParticipantFromChatRoom = update(KEYSPACE,CHATROOMS)
                .with(remove("participants",bindMarker("participant")))
                .where(eq("room_name",bindMarker("room_name")));
        removeParticipantFromChatRoomPs = session.prepare(removeParticipantFromChatRoom);

        // Delete chat room with conditions
        RegularStatement deleteChatRoom = delete().from(KEYSPACE, CHATROOMS)
                .where(eq("room_name", bindMarker("room_name")))
                .onlyIf(eq("creator_login", bindMarker("creator_login")))
                .and(eq("participants", bindMarker("participants")));
        deleteChatRoomPs = session.prepare(deleteChatRoom);

        // List messages
        RegularStatement selectMessage = select().all()
                .from(KEYSPACE, CHATROOM_MESSAGES)
                .where(eq("room_name", bindMarker("room_name")))
                .and(lt("message_id", bindMarker("message_id")))
                .limit(bindMarker("lim"));
        selectMessagePs = session.prepare(selectMessage);

        // Delete all messages from chat room
        RegularStatement deleteMessages = delete().from(KEYSPACE, CHATROOM_MESSAGES).where(QueryBuilder.eq("room_name", bindMarker("roomName")));
        deleteAllMessagePs = session.prepare(deleteMessages);

        // Create persistent token
        RegularStatement createToken = insertInto(KEYSPACE, PERSISTENT_TOKEN)
                .value("series", bindMarker("series"))
                .value("token_value", bindMarker("creator"))
                .value("token_date", bindMarker("creator_login"))
                .value("ip_address", bindMarker("creation_date"))
                .value("user_agent", bindMarker("creation_date"))
                .value("login", bindMarker("banner"))
                .value("pass", bindMarker("pass"))
                .value("authorities", bindMarker("authorities"))
                .using(ttl(bindMarker("ttl")));
        createTokenPs = session.prepare(createToken);

        // Update persistent token value
        RegularStatement updateToken = update(KEYSPACE, PERSISTENT_TOKEN)
                .with(set("token_value",bindMarker("token_value")))
                .where(eq("series",bindMarker("series")))
                .using(ttl(bindMarker("ttl")));
        updateTokenPs = session.prepare(updateToken);
    }
}
