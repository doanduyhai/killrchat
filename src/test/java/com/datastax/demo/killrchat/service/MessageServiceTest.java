package com.datastax.demo.killrchat.service;


import static com.datastax.demo.killrchat.entity.Schema.CHATROOM_MESSAGES;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.KILLRCHAT_LOGIN;
import static com.datastax.demo.killrchat.service.MessageService.JOINING_MESSAGE;
import static com.datastax.demo.killrchat.service.MessageService.LEAVING_MESSAGE;

import static com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static info.archinnov.achilles.generated.meta.entity.MessageEntity_AchillesMeta.author;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.demo.killrchat.entity.MessageEntity;
import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.junit.AchillesTestResource;
import info.archinnov.achilles.junit.AchillesTestResourceBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    @Rule
    public AchillesTestResource<ManagerFactory> resource = AchillesTestResourceBuilder
            .forJunit()
            .createAndUseKeyspace(KEYSPACE)
            .entityClassesToTruncate(MessageEntity.class)
            .truncateBeforeAndAfterTest()
            .build((cluster, statementsCache) -> ManagerFactoryBuilder
                    .builder(cluster)
                    .withStatementsCache(statementsCache)
                    .doForceSchemaCreation(true)
                    .withDefaultKeyspaceName(KEYSPACE)
                    .build());

    private Session session = resource.getNativeSession();
    private MessageService service = new MessageService(resource.getManagerFactory().forMessageEntity());

    private LightUserModel johnDoe = new LightUserModel("jdoe","John","DOE");
    private LightUserModel helenSue = new LightUserModel("hsue","Helen","SUE");

    @Test
    public void should_create_new_chat_message() throws Exception {
        //Given
        LightUserModel johnDoe = new LightUserModel("jdoe","John","DOE");
        String roomName = "games";
        String messageContent = "Starcraft2 is awesome!";

        //When
        service.postNewMessage(johnDoe, roomName, messageContent);

        // Wait for insert async
        Thread.sleep(100);

        //Then
        final Select selectMessages = QueryBuilder.select().from(KEYSPACE, CHATROOM_MESSAGES)
                .where(eq("room_name", roomName))
                .limit(1);

        final Row lastMessage = session.execute(selectMessages).one();

        assertThat(lastMessage.getUUID("message_id")).isNotNull();
        assertThat(author.decodeFromGettable(lastMessage)).isEqualTo(johnDoe);
        assertThat(lastMessage.getString("content")).isEqualTo(messageContent);
        assertThat(lastMessage.getBool("system_message")).isFalse();
    }


    @Test
    public void should_fetch_next_messages_starting_from_now() throws Exception {
        //Given
        String roomName = "games";
        String message1 = "Starcraft2 is awesome!";
        String message2 = "No, WoW is ways better";
        String message3 = "Ok, so let's say Starcraft2 and WoW are the best Blizzard games";
        String message4 = "What's about Diablo 3 ?";
        String message5 = "You're right, completely forgot it!";
        final UUID messageId1 = UUIDs.timeBased();
        final UUID messageId2 = UUIDs.timeBased();
        final UUID messageId3 = UUIDs.timeBased();
        final UUID messageId4 = UUIDs.timeBased();
        final UUID messageId5 = UUIDs.timeBased();

        Insert createMessage = QueryBuilder.insertInto(KEYSPACE, CHATROOM_MESSAGES)
                .value("room_name", bindMarker("room_name"))
                .value("message_id", bindMarker("message_id"))
                .value("author", bindMarker("author"))
                .value("content", bindMarker("content"))
                .value("system_message", bindMarker("system_message"));

        final PreparedStatement preparedStatement = session.prepare(createMessage);


        session.execute(preparedStatement.bind(roomName, messageId1, author.encodeFromJava(johnDoe), message1, false));
        session.execute(preparedStatement.bind(roomName, messageId2, author.encodeFromJava(helenSue), message2, false));
        session.execute(preparedStatement.bind(roomName, messageId3, author.encodeFromJava(johnDoe), message3, false));
        session.execute(preparedStatement.bind(roomName, messageId4, author.encodeFromJava(helenSue), message4, false));
        session.execute(preparedStatement.bind(roomName, messageId5, author.encodeFromJava(johnDoe), message5, false));

        //When
        final List<MessageModel> messages = service.fetchNextMessagesForRoom(roomName, UUIDs.timeBased(), 2);

        //Then
        assertThat(messages).hasSize(2);
        final MessageModel lastMessage = messages.get(1);

        assertThat(lastMessage.getAuthor()).isEqualTo(johnDoe);
        assertThat(lastMessage.getMessageId()).isEqualTo(messageId5);
        assertThat(lastMessage.getContent()).isEqualTo(message5);

        final MessageModel beforeLastMessage = messages.get(0);

        assertThat(beforeLastMessage.getAuthor()).isEqualTo(helenSue);
        assertThat(beforeLastMessage.getMessageId()).isEqualTo(messageId4);
        assertThat(beforeLastMessage.getContent()).isEqualTo(message4);
    }

    @Test
    public void should_fetch_some_message_starting_from_the_last_one_excluding() throws Exception {
        //Given
        String roomName = "games";
        String message1 = "Starcraft2 is awesome!";
        String message2 = "No, WoW is ways better";
        String message3 = "Ok, so let's say Starcraft2 and WoW are the best Blizzard games";
        String message4 = "What's about Diablo 3 ?";
        String message5 = "You're right, completely forgot it!";
        final UUID messageId1 = UUIDs.timeBased();
        Thread.sleep(1);
        final UUID messageId2 = UUIDs.timeBased();
        Thread.sleep(1);
        final UUID messageId3 = UUIDs.timeBased();
        Thread.sleep(1);
        final UUID messageId4 = UUIDs.timeBased();
        Thread.sleep(1);
        final UUID messageId5 = UUIDs.timeBased();

        Insert createMessage = QueryBuilder.insertInto(KEYSPACE, CHATROOM_MESSAGES)
                .value("room_name", bindMarker("room_name"))
                .value("message_id", bindMarker("message_id"))
                .value("author", bindMarker("author"))
                .value("content", bindMarker("content"))
                .value("system_message", bindMarker("system_message"));

        final PreparedStatement preparedStatement = session.prepare(createMessage);

        session.execute(preparedStatement.bind(roomName, messageId1, author.encodeFromJava(johnDoe), message1, false));
        session.execute(preparedStatement.bind(roomName, messageId2, author.encodeFromJava(helenSue), message2, false));
        session.execute(preparedStatement.bind(roomName, messageId3, author.encodeFromJava(johnDoe), message3, false));
        session.execute(preparedStatement.bind(roomName, messageId4, author.encodeFromJava(helenSue), message4, false));
        session.execute(preparedStatement.bind(roomName, messageId5, author.encodeFromJava(johnDoe), message5, false));

        //When
        final List<MessageModel> messages = service.fetchNextMessagesForRoom(roomName, messageId4, 2);

        //Then
        assertThat(messages).hasSize(2);
        final MessageModel lastMessage = messages.get(1);

        assertThat(lastMessage.getAuthor()).isEqualTo(johnDoe);
        assertThat(lastMessage.getMessageId()).isEqualTo(messageId3);
        assertThat(lastMessage.getContent()).isEqualTo(message3);

        final MessageModel beforeLastMessage = messages.get(0);

        assertThat(beforeLastMessage.getAuthor()).isEqualTo(helenSue);
        assertThat(beforeLastMessage.getMessageId()).isEqualTo(messageId2);
        assertThat(beforeLastMessage.getContent()).isEqualTo(message2);
    }

    @Test
    public void should_create_joining_message() throws Exception {
        //Given
        final String roomName = "games";

        //When
        service.createJoiningMessage(roomName, johnDoe);

        // Wait for insert async
        Thread.sleep(100);

        //Then
        final Select selectMessages = QueryBuilder.select().from(KEYSPACE, CHATROOM_MESSAGES)
                .where(eq("room_name", roomName)).limit(1);

        final Row lastMessage = session.execute(selectMessages).one();

        assertThat(lastMessage.getUUID("message_id")).isNotNull();
        assertThat(author.decodeFromGettable(lastMessage).getLogin()).isEqualTo(KILLRCHAT_LOGIN);
        assertThat(lastMessage.getString("content")).isEqualTo(format(JOINING_MESSAGE, "John DOE"));
        assertThat(lastMessage.getBool("system_message")).isTrue();
    }

    @Test
    public void should_create_leaving_message() throws Exception {
        //Given
        final String roomName = "games";

        //When
        service.createLeavingMessage(roomName, johnDoe);

        // Wait for insert async
        Thread.sleep(100);

        //Then
        final Select selectMessages = QueryBuilder.select().from(KEYSPACE, CHATROOM_MESSAGES)
                .where(eq("room_name", roomName)).limit(1);

        final Row lastMessage = session.execute(selectMessages).one();

        assertThat(lastMessage.getUUID("message_id")).isNotNull();
        assertThat(author.decodeFromGettable(lastMessage).getLogin()).isEqualTo(KILLRCHAT_LOGIN);
        assertThat(lastMessage.getString("content")).isEqualTo(format(LEAVING_MESSAGE, "John DOE"));
        assertThat(lastMessage.getBool("system_message")).isTrue();
    }
}