package com.datastax.demo.killrchat.exceptions;

public class IncorrectOldPasswordException extends RuntimeException{
    public IncorrectOldPasswordException(String message) {
        super(message);
    }
}
