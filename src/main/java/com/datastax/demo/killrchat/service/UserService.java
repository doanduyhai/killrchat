package com.datastax.demo.killrchat.service;

import javax.inject.Inject;
import javax.validation.Valid;

import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.exceptions.RememberMeDoesNotExistException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.security.utils.SecurityUtils;

import info.archinnov.achilles.generated.manager.UserEntity_Manager;

import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
public class UserService {


    private final UserEntity_Manager manager;

    @Inject
    public UserService(UserEntity_Manager manager) {
        this.manager = manager;
    }

    public void createUser(@Valid UserModel model) {
        final UserEntity entity = UserEntity.fromModel(model);
        manager.crud()
                .insert(entity)
                .ifNotExists()
                .withLwtResultListener(lwtResult -> {
                    throw new UserAlreadyExistsException(format("The user with the login '%s' already exists", model.getLogin()));
                })
                .execute();
    }

    public UserEntity findByLogin(String login) {
        final UserEntity entity = manager.crud().findById(login).get();
        if (entity == null) {
            throw new UserNotFoundException(format("Cannot find user with login '%s'", login));
        }
        return entity;
    }

    public UserModel fetchRememberMeUser() {
        final String login = SecurityUtils.getCurrentLogin();
        if ("anonymousUser".equals(login)) {
            throw new RememberMeDoesNotExistException(format("There is no remember me information for the login '%s'", login));
        }
        return findByLogin(login).toModel();
    }
}
