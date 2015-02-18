package com.datastax.demo.killrchat.resource.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomDeletionModel {

    @NotNull
    private Set<String> participants;

    public ChatRoomDeletionModel() {
    }

    public ChatRoomDeletionModel(Set<String> participants) {
        this.participants = participants;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }
}
