package com.datastax.demo.killrchat.model;

import com.datastax.demo.killrchat.json.JsonDateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageModel {

    private UUID messageId;

    @NotNull
    @JsonSerialize(using = JsonDateSerializer.class)
    private Date creationDate;

    @NotNull
    private LightUserModel author;

    @NotBlank
    private String content;

    private boolean systemMessage;

    public MessageModel(UUID messageId, Date creationDate, LightUserModel author, String content, boolean systemMessage) {
        this.messageId = messageId;
        this.creationDate = creationDate;
        this.author = author;
        this.content = content;
        this.systemMessage = systemMessage;
    }

    public MessageModel() {
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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
