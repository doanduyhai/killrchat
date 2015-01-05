package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOMS;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;

@Entity(keyspace = KEYSPACE, table = CHATROOMS)
@Strategy(naming = NamingStrategy.SNAKE_CASE)
public class ChatRoomEntity {

    @PartitionKey
    private String roomName;

    @Column
    @NotNull
    @JSON
    private LightUserModel creator;

    @Column
    private String creatorLogin;

    @NotNull
    @Column
    private Date creationDate;

    @Column
    private String banner;

    @Column
    @EmptyCollectionIfNull
    @JSON
    private Set<LightUserModel> participants;


    public ChatRoomEntity(String roomName, LightUserModel creator, Date creationDate, String banner, Set<LightUserModel> participants) {
        this.roomName = roomName;
        this.creator = creator;
        this.creatorLogin = creator.getLogin();
        this.creationDate = creationDate;
        this.banner = banner;
        this.participants = participants;
    }

    public ChatRoomModel toModel() {
        return new ChatRoomModel(roomName, creator, creationDate, banner, participants);
    }













    /**
     *
     * Boring getters & setters & default constructor
     *
     */
    public ChatRoomEntity() {
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getCreatorLogin() {
        return creatorLogin;
    }

    public void setCreatorLogin(String creatorLogin) {
        this.creatorLogin = creatorLogin;
    }

    public LightUserModel getCreator() {
        return creator;
    }

    public void setCreator(LightUserModel creator) {
        this.creator = creator;
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
}
