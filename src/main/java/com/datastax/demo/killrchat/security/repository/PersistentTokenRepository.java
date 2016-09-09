package com.datastax.demo.killrchat.security.repository;

import com.datastax.demo.killrchat.entity.PersistentTokenEntity;
import info.archinnov.achilles.generated.manager.PersistentTokenEntity_Manager;
import org.springframework.stereotype.Repository;
import javax.inject.Inject;


@Repository
public class PersistentTokenRepository {



    private final PersistentTokenEntity_Manager manager;

    @Inject
    public PersistentTokenRepository(PersistentTokenEntity_Manager manager) {
        this.manager = manager;
    }

    public void insert(PersistentTokenEntity token) {
        final PersistentTokenEntity entity = new PersistentTokenEntity(token.getSeries(), token.getTokenValue(),
                token.getTokenDate(), token.getIpAddress(),
                token.getUserAgent(), token.getLogin(),
                token.getPass(), token.getAuthorities());

        manager
                .crud()
                .insert(entity)
                .execute();
    }

    public void deleteById(String series) {
        manager.crud().deleteById(series).execute();
    }

    public PersistentTokenEntity findById(String presentedSeries) {
        return manager.crud().findById(presentedSeries).get();
    }

    public void update(PersistentTokenEntity token) {
        manager
                .dsl()
                .update()
                .fromBaseTable()
                .tokenValue().Set(token.getTokenValue())
                .where()
                .series().Eq(token.getSeries())
                .usingTimeToLive(PersistentTokenEntity.TOKEN_VALIDITY_SECONDS)
                .execute();
    }
}
