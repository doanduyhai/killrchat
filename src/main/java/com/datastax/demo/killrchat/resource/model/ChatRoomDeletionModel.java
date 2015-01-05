package com.datastax.demo.killrchat.resource.model;

import com.datastax.demo.killrchat.model.LightUserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomDeletionModel {

    @NotNull
    private Set<LightUserModel> participants;

    public ChatRoomDeletionModel() {
    }

    public ChatRoomDeletionModel(Set<LightUserModel> participants) {
        this.participants = participants;
    }

    public Set<LightUserModel> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<LightUserModel> participants) {
        this.participants = participants;
    }
}
