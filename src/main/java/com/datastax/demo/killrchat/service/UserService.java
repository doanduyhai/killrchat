package com.datastax.demo.killrchat.service;


import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.Validator;

import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.exceptions.RememberMeDoesNotExistException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.security.repository.CassandraRepository;
import com.datastax.demo.killrchat.security.utils.SecurityUtils;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
public class UserService {

    @Inject
    Session session;

    @Inject
    CassandraRepository repository;


    public void createUser(@Valid UserModel model) {
        final BoundStatement bs = repository.createUserPs.bind(model.getLogin(), model.getPassword(), model.getFirstname(), model.getLastname(), model.getEmail(), model.getBio());
        final Row lwtResult = session.execute(bs).one();
        if (!lwtResult.getBool("[applied]")) {
            throw new UserAlreadyExistsException(format("The user with the login '%s' already exists", model.getLogin()));
        }
    }

    public UserEntity findByLogin(String login) {
        final UserEntity userEntity = repository.userMapper.get(login);
        if (userEntity == null) {
            throw new UserNotFoundException(format("Cannot find user with login '%s'", login));
        }
        return userEntity;
    }

    public UserModel fetchRememberMeUser() {
        final String login = SecurityUtils.getCurrentLogin();
        if ("anonymousUser".equals(login)) {
            throw new RememberMeDoesNotExistException(format("There is no remember me information for the login '%s'", login));
        }
        return findByLogin(login).toModel();
    }
}
