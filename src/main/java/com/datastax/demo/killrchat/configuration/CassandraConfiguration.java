package com.datastax.demo.killrchat.configuration;

import com.datastax.demo.killrchat.entity.Schema;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder;
import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.script.ScriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

@Configuration
public class CassandraConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CassandraConfiguration.class);

    private static final String CLUSTER_NAME = "killrchat";

    @Inject
    private Environment env;

    @Profile(Profiles.SPRING_PROFILE_DEVELOPMENT)
    @Bean(destroyMethod = "shutDown")
    public ManagerFactory cassandraNativeClusterDev() {
        final Cluster cluster = CassandraEmbeddedServerBuilder
                .builder()
                .cleanDataFilesAtStartup(false)
                .withDataFolder(env.getProperty("dev.cassandra.folders.data"))
                .withCommitLogFolder(env.getProperty("dev.cassandra.folders.commitlog"))
                .withSavedCachesFolder(env.getProperty("dev.cassandra.folders.saved_caches"))
                .withDurableWrite(true)
                .withClusterName(CLUSTER_NAME)
                .buildNativeCluster();
        final Session session = cluster.connect();

        maybeCreateSchema(session);
        return ManagerFactoryBuilder
                .builder(cluster)
                .withDefaultKeyspaceName(Schema.KEYSPACE)
                .doForceSchemaCreation(true)
                .withBeanValidation(true)
                .withPostLoadBeanValidation(true)
                .build();

    }

    @Profile(Profiles.SPRING_PROFILE_PRODUCTION)
    @Bean(destroyMethod = "shutDown")
    public ManagerFactory cassandraNativeClusterProduction() {

        Cluster cluster = Cluster.builder()
                .addContactPoints(env.getProperty("cassandra.host"))
                .withPort(Integer.parseInt(env.getProperty("cassandra.cql.port")))
                .withClusterName(CLUSTER_NAME)
                .build();

        final ManagerFactory factory = ManagerFactoryBuilder
                .builder(cluster)
                .build();
        final Session session = cluster.connect();

        maybeCreateSchema(session);
        return factory;
    }

    private void maybeCreateSchema(Session session) {
        logger.info("Execute schema creation script 'cassandra/schema_creation.cql' if necessary");
        final ScriptExecutor scriptExecutor = new ScriptExecutor(session);
        scriptExecutor.executeScript("cassandra/schema_creation.cql");

    }
}
