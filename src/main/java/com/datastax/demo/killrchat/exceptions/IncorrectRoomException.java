package com.datastax.demo.killrchat.exceptions;

public class IncorrectRoomException extends RuntimeException{
    public IncorrectRoomException(String message) {
        super(message);
    }
}
