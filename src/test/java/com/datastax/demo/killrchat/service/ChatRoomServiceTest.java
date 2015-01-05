package com.datastax.demo.killrchat.service;

import static com.datastax.demo.killrchat.entity.Schema.*;
import static com.datastax.demo.killrchat.service.ChatRoomService.DELETION_MESSAGE;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.*;

import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.exceptions.ChatRoomAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.ChatRoomDoesNotExistException;
import com.datastax.demo.killrchat.exceptions.IncorrectRoomException;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Sets;
import info.archinnov.achilles.junit.AchillesResource;
import info.archinnov.achilles.junit.AchillesResourceBuilder;
import info.archinnov.achilles.script.ScriptExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ChatRoomServiceTest {

    @Rule
    public AchillesResource resource = AchillesResourceBuilder
            .withEntityPackages(UserEntity.class.getPackage().getName())
            .withKeyspaceName(KEYSPACE)
            .withBeanValidation()
            .tablesToTruncate(USERS, CHATROOMS)
            .truncateBeforeAndAfterTest().build();

    private Session session = resource.getNativeSession();

    private ScriptExecutor scriptExecutor = resource.getScriptExecutor();

    private ChatRoomService service = new ChatRoomService();

    private LightUserModel john = new LightUserModel("jdoe", "John", "DOE");
    private LightUserModel helen = new LightUserModel("hsue", "Helen", "SUE");

    private String johnAsJson;
    private String helenAsJson;

    @Before
    public void setUp() throws IOException {
        service.manager = resource.getPersistenceManager();
        johnAsJson = service.manager.serializeToJSON(john);
        helenAsJson = service.manager.serializeToJSON(helen);
    }

    @Test
    public void should_create_chat_room() throws Exception {
        //Given
        final String roomName = "random_thoughts";

        //When
        service.createChatRoom(roomName, "banner", john);

        //Then
        final Row chatRoom = session.execute(select().from(KEYSPACE, CHATROOMS).where(eq("room_name", "random_thoughts"))).one();
        final Row jdoeChatRooms = session.execute(select("chat_rooms").from(KEYSPACE, USERS).where(eq("login", "jdoe"))).one();

        assertThat(chatRoom).isNotNull();
        assertThat(chatRoom.getString("room_name")).isEqualTo(roomName);
        assertThat(chatRoom.getString("banner")).isEqualTo("banner");
        assertThat(chatRoom.getSet("participants", String.class)).contains(johnAsJson);

        assertThat(jdoeChatRooms.getSet("chat_rooms", String.class)).hasSize(1).containsExactly(roomName);
    }

    @Test(expected = ChatRoomAlreadyExistsException.class)
    public void should_exception_when_creating_existing_chat_room() throws Exception {
        //Given
        scriptExecutor.executeScript("should_exception_when_creating_existing_chat_room.cql");

        //When
        service.createChatRoom("all", "banner", new LightUserModel("jdoe","John", "DOE"));
    }

    @Test
    public void should_find_room_by_name() throws Exception {
        //Given
        scriptExecutor.executeScript("should_find_room_by_name.cql");

        //When
        final ChatRoomModel model = service.findRoomByName("games");

        //Then
        assertThat(model.getCreator()).isEqualTo(john);
        assertThat(model.getRoomName()).isEqualTo("games");
        assertThat(model.getParticipants()).isEmpty();
    }

    @Test(expected = ChatRoomDoesNotExistException.class)
    public void should_exception_when_room_does_not_exist() throws Exception {
        service.findRoomByName("games");
    }


    @Test
    public void should_list_chat_rooms_with_limits() throws Exception {
        //Given
        scriptExecutor.executeScript("should_list_chat_rooms_with_limits.cql");

        //When
        final List<ChatRoomModel> rooms = service.listChatRooms(3);

        //Then
        assertThat(rooms).hasSize(3);
        assertThat(rooms.get(0).getRoomName()).isEqualTo("saas");
        assertThat(rooms.get(1).getRoomName()).isEqualTo("bioshock");
        assertThat(rooms.get(2).getRoomName()).isEqualTo("java");
    }

    @Test
    public void should_add_user_to_chat_room() throws Exception {
        //Given
        scriptExecutor.executeScript("should_add_user_to_chat_room.cql");

        //When
        service.addUserToRoom("politics", helen);

        //Then
        final Select.Where participants = select("participants").from(KEYSPACE, CHATROOMS).where(eq("room_name", "politics"));
        final Select.Where helenChatRooms = select("chat_rooms").from(KEYSPACE, USERS).where(eq("login", "hsue"));

        final Row participantsRow = session.execute(participants).one();
        final Row helenChatRoomsRow = session.execute(helenChatRooms).one();

        assertThat(participantsRow.getSet("participants",String.class)).containsOnly(johnAsJson, helenAsJson);
        assertThat(helenChatRoomsRow.getSet("chat_rooms",String.class)).hasSize(1).containsExactly("politics");
    }

    @Test(expected = ChatRoomDoesNotExistException.class)
    public void should_exception_when_adding_user_to_non_existing_chat_room() throws Exception {
        service.addUserToRoom("politics", helen);
    }

    @Test
    public void should_remove_user_from_chat_room() throws Exception {
        //Given
        scriptExecutor.executeScript("should_remove_user_from_chat_room.cql");

        //When
        service.removeUserFromRoom("politics", helen);

        //Then
        final Select.Where participants = select("participants").from(KEYSPACE, CHATROOMS).where(eq("room_name", "politics"));
        final Select.Where helenChatRooms = select("chat_rooms").from(KEYSPACE, USERS).where(eq("login", "hsue"));

        final Row participantsRow = session.execute(participants).one();
        final Row helenChatRoomsRow = session.execute(helenChatRooms).one();

        assertThat(participantsRow).isNotNull();
        assertThat(participantsRow.getSet("participants", String.class)).containsOnly(johnAsJson);

        assertThat(helenChatRoomsRow.getSet("chat_rooms",String.class)).hasSize(0);
    }

    @Test
    public void should_remove_chat_room_with_all_participants() throws Exception {
        //Given
        scriptExecutor.executeScript("should_remove_chat_room_with_all_participants.cql");

        //When
        final String message = service.deleteRoomWithParticipants("jdoe", "fairy", Sets.newHashSet("jdoe", "hsue", "alice", "bob"));

        //Then
        assertThat(message).isEqualTo(format(DELETION_MESSAGE, "fairy", "jdoe"));

        final Row room = session.execute(select().from(KEYSPACE, CHATROOMS).where(eq("room_name", "fairy"))).one();
        assertThat(room).isNull();

        final List<Row> messages = session.execute(select().from(KEYSPACE, CHATROOM_MESSAGES).where(eq("room_name", "fairy")).limit(10)).all();

        assertThat(messages).isEmpty();

        final Row jdoeRooms = session.execute(select("chat_rooms").from(KEYSPACE, USERS).where(eq("login", "jdoe"))).one();
        final Row hsueRooms = session.execute(select("chat_rooms").from(KEYSPACE, USERS).where(eq("login", "hsue"))).one();
        final Row aliceRooms = session.execute(select("chat_rooms").from(KEYSPACE, USERS).where(eq("login", "alice"))).one();
        final Row bobRooms = session.execute(select("chat_rooms").from(KEYSPACE, USERS).where(eq("login", "bob"))).one();

        assertThat(jdoeRooms.isNull("chat_rooms")).isTrue();
        assertThat(hsueRooms.isNull("chat_rooms")).isTrue();
        assertThat(aliceRooms.isNull("chat_rooms")).isTrue();
        assertThat(bobRooms.isNull("chat_rooms")).isTrue();


    }

    @Test(expected = IncorrectRoomException.class)
    public void should_fails_to_delete_room_if_not_author() throws Exception {
        //Given
        scriptExecutor.executeScript("should_remove_chat_room_with_all_participants.cql");

        //When
        service.deleteRoomWithParticipants("hsue", "fairy", Sets.newHashSet("jdoe", "hsue", "alice", "bob"));
    }
}
