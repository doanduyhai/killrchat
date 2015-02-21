package com.datastax.demo.killrchat.service;


import javax.inject.Inject;

import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.exceptions.RememberMeDoesNotExistException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.security.utils.SecurityUtils;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
public class UserService {

    @Inject
    PersistenceManager manager;

    public void createUser(UserModel model) {
        try {
            manager.insert(UserEntity.fromModel(model), OptionsBuilder.ifNotExists());
        } catch (AchillesLightWeightTransactionException ex) {
            throw new UserAlreadyExistsException(format("The user with the login '%s' already exists", model.getLogin()));
        }
    }

    public UserEntity findByLogin(String login) {
        final UserEntity userEntity = manager.find(UserEntity.class, login);
        if (userEntity == null) {
            throw new UserNotFoundException(format("Cannot find user with login '%s'", login));
        }
        return userEntity;
    }

    /**
     * This method is already implemented, nothing related to Cassandra
     */
    public UserModel fetchRememberMeUser() {
        final String login = SecurityUtils.getCurrentLogin();
        if ("anonymousUser".equals(login)) {
            throw new RememberMeDoesNotExistException(format("There is no remember me information for the login '%s'", login));
        }
        return findByLogin(login).toModel();
    }
}