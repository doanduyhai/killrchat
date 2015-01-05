package com.datastax.demo.killrchat.configuration;

import com.datastax.demo.killrchat.entity.Schema;
import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.driver.core.Cluster;
import info.archinnov.achilles.listener.LWTResultListener;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.persistence.PersistenceManagerFactory;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.List;

import static com.datastax.demo.killrchat.entity.Schema.KILLRCHAT_LOGIN;
import static com.datastax.demo.killrchat.entity.Schema.KILLRCHAT_USER;
import static info.archinnov.achilles.persistence.PersistenceManagerFactory.PersistenceManagerFactoryBuilder;
import static info.archinnov.achilles.type.OptionsBuilder.ifNotExists;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@Configuration
public class AchillesConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AchillesConfiguration.class);

    @Inject
    private Cluster cluster;

    @Inject
    private Environment env;

    @Bean(destroyMethod = "shutDown")
    public PersistenceManager getPersistenceManager() {


        final List<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        boolean isProduction = activeProfiles.contains(Profiles.SPRING_PROFILE_PRODUCTION);
        PersistenceManagerFactory pmFactory = PersistenceManagerFactoryBuilder
                .builder(cluster)
                .withEntityPackages(UserEntity.class.getPackage().getName())
                .withDefaultReadConsistency(ConsistencyLevel.ONE)
                .withDefaultWriteConsistency(ConsistencyLevel.ONE)
                .withKeyspaceName(Schema.KEYSPACE)
                .withExecutorServiceMinThreadCount(5)
                .withExecutorServiceMaxThreadCount(10)
                .forceTableCreation(isProduction ? false : true)
                .build();

        final PersistenceManager pm = pmFactory.createPersistenceManager();

        pm.insert(UserEntity.fromModel(KILLRCHAT_USER), OptionsBuilder.ifNotExists().lwtResultListener(new LWTResultListener() {
            @Override
            public void onSuccess() {
                logger.info("Create new administration 'killrchat' account");
            }

            @Override
            public void onError(LWTResult lwtResult) {
                logger.debug("Administration 'killrchat' account already exists");
            }
        }));

        return pm;
    }
}
