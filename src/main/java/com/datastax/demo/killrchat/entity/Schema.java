package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.UserModel;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;

public interface Schema {
    static String KEYSPACE = "killrchat";
    static String USERS = "users";
    static String CHATROOMS = "chat_rooms";
    static String CHATROOM_MESSAGES = "chat_room_messages";
    static String PERSISTENT_TOKEN = "security_tokens";
    static String KILLRCHAT_LOGIN = "killrchat";
    static UserModel KILLRCHAT_USER = new UserModel(KILLRCHAT_LOGIN, RandomStringUtils.randomAlphanumeric(10),
            KILLRCHAT_LOGIN, KILLRCHAT_LOGIN,
            "Administrative account", "");

}
