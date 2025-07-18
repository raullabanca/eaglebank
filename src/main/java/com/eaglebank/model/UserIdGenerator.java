package com.eaglebank.model;

import java.io.Serializable;
import java.util.UUID;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class UserIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        final String randomAlphanumeric = UUID.randomUUID()
                .toString()
                .replaceAll("-", "")
                .substring(0, 12);
        return "usr-" + randomAlphanumeric;
    }
}
