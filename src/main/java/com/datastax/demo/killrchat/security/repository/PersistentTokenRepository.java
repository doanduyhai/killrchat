package com.datastax.demo.killrchat.security.repository;

import com.datastax.demo.killrchat.entity.PersistentTokenEntity;
import com.datastax.demo.killrchat.security.authority.UserAuthority;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UDTValue;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.collect.FluentIterable.from;
import static info.archinnov.achilles.type.OptionsBuilder.withTtl;

@Repository
public class PersistentTokenRepository {

    private Function<UserAuthority,UDTValue> TO_UDT = new Function<UserAuthority, UDTValue>() {
        @Nullable
        @Override
        public UDTValue apply(UserAuthority input) {
            return repository.userAuthorityUdtMapper.toUDT(input);

        }
    };

    @Inject
    Session session;

    @Inject
    CassandraRepository repository;

    public static final int TOKEN_VALIDITY_DAYS = 31;

    public static final int TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * TOKEN_VALIDITY_DAYS;

    public void insert(PersistentTokenEntity token) {
        final BoundStatement bs = repository.createTokenPs.bind(token.getSeries(),
                token.getTokenValue(),
                token.getTokenDate(),
                token.getIpAddress(),
                token.getUserAgent(),
                token.getLogin(),
                token.getPass(),
                from(token.getAuthorities()).transform(TO_UDT).toSet(),
                TOKEN_VALIDITY_SECONDS);
        session.execute(bs);
    }

    public void deleteById(String series) {
        repository.persistentTokenMapper.delete(series);
    }

    public PersistentTokenEntity findById(String presentedSeries) {
        return repository.persistentTokenMapper.get(presentedSeries);
    }

    public void update(PersistentTokenEntity token) {
        final BoundStatement bs = repository.updateTokenPs.bind(token.getTokenValue(), token.getSeries(), TOKEN_VALIDITY_SECONDS);
        session.execute(bs);
    }
}
