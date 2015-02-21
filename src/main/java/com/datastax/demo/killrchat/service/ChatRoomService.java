package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;

import info.archinnov.achilles.persistence.PersistenceManager;

import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;


@Service
public class ChatRoomService {


    @Inject
    PersistenceManager manager;


    public void createChatRoom(String roomName, String banner, LightUserModel creator) {

    }

    public ChatRoomModel findRoomByName(String roomName) {
        return null;
    }

    public List<ChatRoomModel> listChatRooms(int fetchSize) {
        return null;
    }

    public void addUserToRoom(String roomName, LightUserModel participant) {

    }

    public void removeUserFromRoom(String roomName, LightUserModel participant) {

    }

    public String deleteRoomWithParticipants(String creatorLogin, String roomName, Set<String> participants) {
        return null;
    }

}