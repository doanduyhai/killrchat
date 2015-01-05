package com.datastax.demo.killrchat.exceptions;

public class RememberMeDoesNotExistException extends RuntimeException{
    public RememberMeDoesNotExistException(String message) {
        super(message);
    }
}
