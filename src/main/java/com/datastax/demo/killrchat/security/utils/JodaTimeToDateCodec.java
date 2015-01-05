package com.datastax.demo.killrchat.security.utils;

import info.archinnov.achilles.codec.Codec;
import info.archinnov.achilles.exception.AchillesTranscodingException;
import org.joda.time.DateTime;

import java.util.Date;

public class JodaTimeToDateCodec implements Codec<DateTime,Date> {
    @Override
    public Class<DateTime> sourceType() {
        return DateTime.class;
    }

    @Override
    public Class<Date> targetType() {
        return Date.class;
    }

    @Override
    public Date encode(DateTime fromJava) throws AchillesTranscodingException {
        return fromJava.toDate();
    }

    @Override
    public DateTime decode(Date fromCassandra) throws AchillesTranscodingException {
        return new DateTime(fromCassandra);
    }
}
