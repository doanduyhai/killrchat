package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.security.repository.CassandraRepository;
import info.archinnov.achilles.junit.AchillesResource;
import org.junit.rules.ExternalResource;

public class CassandraRepositoryRule extends ExternalResource {

    private static CassandraRepository repository;

    public CassandraRepositoryRule(AchillesResource resource) {
        synchronized (this) {
            if (repository == null) {
                repository = new CassandraRepository();
                repository.session = resource.getNativeSession();
                repository.generateMappersAndPreparedStatements();
            }
        }
    }

    public CassandraRepository getRepository() {
        return repository;
    }
}
