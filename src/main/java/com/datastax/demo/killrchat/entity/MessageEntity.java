package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOM_MESSAGES;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;


@Table(keyspace = KEYSPACE, name = CHATROOM_MESSAGES)
public class MessageEntity {

    @PartitionKey
    @Column(name = "room_name")
    private String roomName;

    @ClusteringColumn
    @Column(name = "message_id")
    private UUID messageId;

    @NotNull
    @Column
    @Frozen
    private LightUserModel author;

    @NotEmpty
    @Column
    private String content;

    @Column(name = "system_message")
    private boolean systemMessage;


    public MessageEntity(String roomName, UUID messageId, LightUserModel author, String content) {
        this.roomName = roomName;
        this.messageId = messageId;
        this.author = author;
        this.content = content;
        this.systemMessage = false;
    }

    public MessageEntity(String roomName, UUID messageId, LightUserModel author, String content, boolean systemMessage) {
        this.roomName = roomName;
        this.messageId = messageId;
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


















    /**
     *
     * Boring getters & setters & default constructor
     *
     */
    public MessageEntity() {
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
