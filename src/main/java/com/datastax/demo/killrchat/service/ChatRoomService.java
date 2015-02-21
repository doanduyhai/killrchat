package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.ChatRoomEntity;
import com.datastax.demo.killrchat.exceptions.ChatRoomAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.ChatRoomDoesNotExistException;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;

import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.PersistenceManager;

import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOMS;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;


@Service
public class ChatRoomService {

    private static final Select SELECT_ROOMS = select().from(KEYSPACE, CHATROOMS).limit(bindMarker("fetchSize"));

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

        /**
         * Specs
         *
         *  Step 1:
         *
         *  - create a new ChatRoomEntity with appropriate properties.
         *    Do not forget to include the creator as the first chat room participant
         *    Chat room creation date = current system date
         *
         *  - insert a new chat room using LightWeight Transaction to avoid creating twice the same room
         *    (see UserService.createUser())
         *
         *  - catch the AchillesLightWeightTransactionException and rethrow an ChatRoomAlreadyExistsException
         *
         */

        try {

            //Implement the service here

        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomAlreadyExistsException(String.format("The room '%s' already exists", roomName));
        }

        /**  Step 2:
         *
         *  - add this room to the creator's list of rooms in the UserEntity.chatRooms property.
         *    For this, instead of doing a read-before-write, which is an anti-pattern in Cassandra, use the update
         *    by proxy API of Achilles:
         *
         *      Ex:
         *
         *      MyEntity proxy = manager.forUpdate(MyEntity.class, primaryKey);
         *
         *      //update of collection or map
         *      proxy.getList().add(...);
         *      proxy.getSet().add(..);
         *      proxy.getMap().put(key,value);
         *
         *      manager.update(proxy);
         *
         *      For collections and maps, Achilles returns a ListWrapper/SetWrapper/MapWrapper that intercept any
         *      mutation and transform them into appropriate UPDATE statements.
         *      No SELECT is issued by Achilles when using manager.forUpdate() API
         *
         *  - for more details about the Direct Update API (optional): https://github.com/doanduyhai/Achilles/wiki/Direct-Update-Proxy
         *
         */

    }

    public ChatRoomModel findRoomByName(String roomName) {
        final ChatRoomEntity chatRoom = manager.find(ChatRoomEntity.class, roomName);
        if (chatRoom == null) {
            throw new ChatRoomDoesNotExistException(String.format("Chat room '%s' does not exists", roomName));
        }
        return chatRoom.toModel();
    }

    public List<ChatRoomModel> listChatRooms(int fetchSize) {

        /**
         * Specs
         *
         *  - we must return 'n' chat rooms. For this, use the predefined SELECT_ROOMS statement above. We will
         *    fetch all rooms in the database and limit to 'fetchSize' to avoid OOM exception
         *
         *  - use Achilles Typed Query API. Basically you will inject your own SELECT statement with bound values and
         *    Achilles will execute it and map the returned ResultSet into entities.
         *
         *      Ex:
         *
         *      List<MyEntity> foundEntities = manager.typedQuery(MyEntity.class,
         *                                          "SELECT ... FROM ... WHERE pk = :value1 AND xxx = :value2",
         *                                          new Object[]{boundValue1, boundValue2, ...}).get();
         *
         *  - the SELECT statement is already given for you, check the SELECT_ROOMS attribute defined above
         *
         *  - convert the list of entities to models using the predefined CHAT_ROOM_TO_MODEL function above:
         *
         *      FluentIterable.from(foundEntities).transform(CHAT_ROOM_TO_MODEL). ....
         *
         * - for more details about the Typed Query API (optional): https://github.com/doanduyhai/Achilles/wiki/Queries#typed-query
         */

        return null;
    }

    public void addUserToRoom(String roomName, LightUserModel participant) {

        /**
         * Do not implement, this is for exercise 3
         */

    }

    public void removeUserFromRoom(String roomName, LightUserModel participant) {

        /**
         * Do not implement, this is for exercise 3
         */

    }

    public String deleteRoomWithParticipants(String creatorLogin, String roomName, Set<String> participants) {

        /**
         * Do not implement, this is for exercise 3
         */

        return null;
    }

}