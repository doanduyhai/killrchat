package com.datastax.demo.killrchat.resource;

import javax.validation.ValidationException;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Validator {

    public static void validateNotBlank(String toBeValidated, String message, Object ... args) {
        if (isBlank(toBeValidated)) {
            throw new ValidationException(format(message, args));
        }
    }
}
