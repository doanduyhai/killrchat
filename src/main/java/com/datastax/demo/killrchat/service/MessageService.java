package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    @Inject
    PersistenceManager manager;

    public MessageModel postNewMessage(LightUserModel author, String roomName, String messageContent) {
        return null;
    }

    public List<MessageModel> fetchNextMessagesForRoom(String roomName, UUID fromMessageId, int pageSize) {
        return null;
    }

    public MessageModel createJoiningMessage(String roomName, LightUserModel participant) {
        return null;
    }

    public MessageModel createLeavingMessage(String roomName, LightUserModel participant) {
        return null;
    }
}