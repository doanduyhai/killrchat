package com.datastax.demo.killrchat.service;

import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Rule;
import org.junit.Test;

import com.datastax.demo.killrchat.entity.CompositePartitionKeyEntity;

import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.generated.function.SystemFunctions;
import info.archinnov.achilles.generated.manager.CompositePartitionKeyEntity_Manager;
import info.archinnov.achilles.generated.meta.entity.CompositePartitionKeyEntity_AchillesMeta;
import info.archinnov.achilles.junit.AchillesTestResource;
import info.archinnov.achilles.junit.AchillesTestResourceBuilder;
import info.archinnov.achilles.type.TypedMap;

public class NewTest {

    @Rule
    public AchillesTestResource<ManagerFactory> resource = AchillesTestResourceBuilder
            .forJunit()
            .createAndUseKeyspace(KEYSPACE)
            .entityClassesToTruncate(CompositePartitionKeyEntity.class)
            .truncateBeforeAndAfterTest()
            .build((cluster, statementsCache) -> ManagerFactoryBuilder
                    .builder(cluster)
                    .withStatementsCache(statementsCache)
                    .doForceSchemaCreation(true)
                    .withDefaultKeyspaceName(KEYSPACE)
                    .build());

    @Test
    public void should_test() throws Exception {
        //Given
        CompositePartitionKeyEntity_Manager manager = resource.getManagerFactory().forCompositePartitionKeyEntity();
        Long id = RandomUtils.nextLong(0L, Long.MAX_VALUE);
        UUID uuid = UUID.randomUUID();
        String value = RandomStringUtils.randomAlphabetic(10);

        CompositePartitionKeyEntity entity = new CompositePartitionKeyEntity(id, uuid, value);

        manager.crud().insert(entity).execute();

        //When
        TypedMap typedMap = manager
                .dsl()
                .select()
                .value()
                .function(SystemFunctions.token(CompositePartitionKeyEntity_AchillesMeta.COLUMNS.PARTITION_KEYS), "tokens")
                .fromBaseTable()
                .where()
                .id().Eq(id)
                .uuid().Eq(uuid)
                .getTypedMap();


        //Then
        assertThat(typedMap).isNotNull();
        assertThat(typedMap).isNotEmpty();
        assertThat(typedMap.<Long>getTyped("tokens")).isNotNull();
    }
}
