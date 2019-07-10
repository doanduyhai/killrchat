package com.datastax.demo.killrchat.entity;

import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;

import java.util.UUID;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Immutable;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;

@Table(keyspace = KEYSPACE, table = "composite_partitions")
@Immutable
public class CompositePartitionKeyEntity {

    @PartitionKey(1)
    public final Long id;

    @PartitionKey(2)
    public final UUID uuid;

    @Column
    public final String value;


    public CompositePartitionKeyEntity(Long id, UUID uuid, String value) {
        this.id = id;
        this.uuid = uuid;
        this.value = value;
    }
}
