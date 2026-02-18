package com.sfronc.hub.common;

import java.util.UUID;

public final class Ids {
    private Ids() {}

    public static String correlationId() {
        return UUID.randomUUID().toString();
    }
}
