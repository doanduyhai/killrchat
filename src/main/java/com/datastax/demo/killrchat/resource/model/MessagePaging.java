package com.datastax.demo.killrchat.resource.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessagePaging {

    private UUID fromMessageId;

    private int fetchSize;

    public MessagePaging() {
    }

    public UUID getFromMessageId() {
        return fromMessageId;
    }

    public void setFromMessageId(UUID fromMessageId) {
        this.fromMessageId = fromMessageId;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
}
