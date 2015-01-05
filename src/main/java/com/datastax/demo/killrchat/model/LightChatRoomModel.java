package com.datastax.demo.killrchat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)
public class LightChatRoomModel {

    @NotBlank
    protected String roomName;

    @NotNull
    protected LightUserModel creator;

    public LightChatRoomModel(String roomName, LightUserModel creator) {
        this.roomName = roomName;
        this.creator = creator;
    }

    public LightChatRoomModel() {
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public LightUserModel getCreator() {
        return creator;
    }

    public void setCreator(LightUserModel creator) {
        this.creator = creator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LightChatRoomModel that = (LightChatRoomModel) o;

        return Objects.equals(this.roomName, that.roomName)
                && Objects.equals(this.creator, that.creator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.roomName, this.creator);
    }
}
