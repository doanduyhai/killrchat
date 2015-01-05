package com.datastax.demo.killrchat.security.repository;

import com.datastax.demo.killrchat.entity.PersistentTokenEntity;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

import static info.archinnov.achilles.type.OptionsBuilder.withTtl;

@Repository
public class PersistentTokenRepository {

    @Inject
    private PersistenceManager manager;

    public static final int TOKEN_VALIDITY_DAYS = 31;

    public static final int TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * TOKEN_VALIDITY_DAYS;

    public void insert(PersistentTokenEntity token) {
        manager.insert(token, withTtl(TOKEN_VALIDITY_SECONDS));
    }

    public void deleteById(String series) {
        manager.deleteById(PersistentTokenEntity.class, series);

    }

    public PersistentTokenEntity findById(String presentedSeries) {
        return manager.find(PersistentTokenEntity.class, presentedSeries);
    }

    public void update(PersistentTokenEntity token) {
        manager.update(token, withTtl(TOKEN_VALIDITY_SECONDS));
    }
}
