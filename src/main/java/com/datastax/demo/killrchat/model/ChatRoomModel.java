package com.datastax.demo.killrchat.model;

import com.datastax.demo.killrchat.json.JsonDateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomModel extends LightChatRoomModel{

    @NotNull
    @JsonSerialize(using = JsonDateSerializer.class)
    private Date creationDate;

    private String banner;

    private Set<LightUserModel> participants;

    public ChatRoomModel(String roomName, LightUserModel creator, Date creationDate, String banner, Set<LightUserModel> participants) {
        super(roomName, creator);
        this.creationDate = creationDate;
        this.banner = banner;
        this.participants = participants;
    }

    public ChatRoomModel() {
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }


    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public Set<LightUserModel> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<LightUserModel> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoomModel that = (ChatRoomModel) o;

        return Objects.equals(this.roomName, that.roomName)
                && Objects.equals(this.creator, that.creator)
                && Objects.equals(this.participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.roomName, this.creator, this.participants);
    }

    public static enum Action {
        DELETE
    }
}
