package com.datastax.demo.killrchat.service;


import javax.inject.Inject;

import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.exceptions.RememberMeDoesNotExistException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.security.utils.SecurityUtils;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
public class UserService {

    @Inject
    PersistenceManager manager;

    public void createUser(UserModel model) {
        /**
         * Specs
         *
         *  - use manager.insert() API
         *  - to convert a model to an entity, use the helper static method UserEntity.fromModel()
         *  - use Lightweight Transaction to avoid inserting twice the same user
         *    - the IF NOT EXISTS clause is defined using OptionsBuilder:
         *
         *      manager.insert(???, OptionsBuilder.???());
         *
         *  - if insert using Lightweight Transaction fails, Achilles will throw an AchillesLightWeightTransactionException,
         *    catch it and rethrow an UserAlreadyExistsException
         *
         *  Remark
         *  - for documentation on manager API (optional): https://github.com/doanduyhai/Achilles/wiki/Persistence-Manager-Operations
         *  - for documentation on LightWeight Transaction API (optional): https://github.com/doanduyhai/Achilles/wiki/Lightweight-Transaction
         */
    }

    public UserEntity findByLogin(String login) {

        /**
         * Specs
         *
         *  - use manager.find(???.class, partitionKey)
         *  - if no user is found, throw an UserNotFoundException
         *
         *  Remark
         *  - for documentation on manager API(optional): https://github.com/doanduyhai/Achilles/wiki/Persistence-Manager-Operations
         */

        return null;
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