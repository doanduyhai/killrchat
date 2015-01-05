package com.datastax.demo.killrchat.exceptions;

public class ChatRoomDoesNotExistException extends RuntimeException{
    public ChatRoomDoesNotExistException(String message) {
        super(message);
    }
}
