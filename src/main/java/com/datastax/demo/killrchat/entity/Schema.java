package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.UserModel;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;

public interface Schema {
    String KEYSPACE = "killrchat";
    String USERS = "users";
    String USER_UDT = "user";
    String USER_AUTHORITY_UDT = "user_authority";
    String CHATROOMS = "chat_rooms";
    String CHATROOM_MESSAGES = "chat_room_messages";
    String PERSISTENT_TOKEN = "security_tokens";
    String KILLRCHAT_LOGIN = "killrchat";
    UserModel KILLRCHAT_USER = new UserModel(KILLRCHAT_LOGIN, RandomStringUtils.randomAlphanumeric(10),
            KILLRCHAT_LOGIN, KILLRCHAT_LOGIN,
            "Administrative account", "");

}
