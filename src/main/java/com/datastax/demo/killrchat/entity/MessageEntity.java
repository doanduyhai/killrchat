package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.utils.UUIDs;
import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOM_MESSAGES;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;

@Entity(keyspace = KEYSPACE, table = CHATROOM_MESSAGES)
@Strategy(naming = NamingStrategy.SNAKE_CASE)
public class MessageEntity {

    @CompoundPrimaryKey
    private CompoundPk primaryKey;

    @NotNull
    @JSON
    @Column
    private LightUserModel author;

    @NotEmpty
    @Column
    private String content;

    @Column
    private boolean systemMessage;


    public MessageEntity(String roomName, UUID messageId, LightUserModel author, String content) {
        this.primaryKey = new CompoundPk(roomName, messageId);
        this.author = author;
        this.content = content;
        this.systemMessage = false;
    }

    public MessageEntity(String roomName, UUID messageId, LightUserModel author, String content, boolean systemMessage) {
        this.primaryKey = new CompoundPk(roomName, messageId);
        this.author = author;
        this.content = content;
        this.systemMessage = systemMessage;
    }

    public MessageModel toModel() {
        MessageModel model = new MessageModel();
        model.setAuthor(this.author);
        model.setContent(this.content);
        model.setMessageId(this.getMessageId());
        model.setSystemMessage(this.systemMessage);
        model.setCreationDate(new Date(UUIDs.unixTimestamp(this.getMessageId())));
        return model;
    }

    public static class CompoundPk {

        @PartitionKey
        private String roomName;

        @ClusteringColumn(value = 1, reversed = true)
        @TimeUUID
        private UUID messageId;

        public CompoundPk(String roomName, UUID messageId) {
            this.roomName = roomName;
            this.messageId = messageId;
        }













        /**
         *
         * Boring getters & setters & default constructor
         *
         */
        public CompoundPk() {
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public UUID getMessageId() {
            return messageId;
        }

        public void setMessageId(UUID messageId) {
            this.messageId = messageId;
        }
    }
















    /**
     *
     * Boring getters & setters & default constructor
     *
     */
    public MessageEntity() {
    }

    public String getRoomName() {
        return primaryKey.roomName;
    }

    public UUID getMessageId() {
        return primaryKey.messageId;
    }

    public CompoundPk getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(CompoundPk primaryKey) {
        this.primaryKey = primaryKey;
    }

    public LightUserModel getAuthor() {
        return author;
    }

    public void setAuthor(LightUserModel author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(boolean systemMessage) {
        this.systemMessage = systemMessage;
    }

}
