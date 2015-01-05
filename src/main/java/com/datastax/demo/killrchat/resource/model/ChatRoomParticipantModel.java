package com.datastax.demo.killrchat.resource.model;

import com.datastax.demo.killrchat.model.LightUserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomParticipantModel {

    @NotNull
    private LightUserModel participant;

    public ChatRoomParticipantModel() {
    }

    public ChatRoomParticipantModel(LightUserModel participant) {
        this.participant = participant;
    }


    public LightUserModel getParticipant() {
        return participant;
    }

    public void setParticipant(LightUserModel participant) {
        this.participant = participant;
    }

    public static enum Status {
        JOIN, LEAVE
    }

}
