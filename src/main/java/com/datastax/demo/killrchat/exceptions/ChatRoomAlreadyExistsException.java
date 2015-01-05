package com.datastax.demo.killrchat.exceptions;

public class ChatRoomAlreadyExistsException extends RuntimeException{
    public ChatRoomAlreadyExistsException(String message) {
        super(message);
    }
}
