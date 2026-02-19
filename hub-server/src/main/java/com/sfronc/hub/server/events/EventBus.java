package com.sfronc.hub.server.events;

import java.util.function.Consumer;

public interface EventBus {
    <E> void subscribe(Class<E> type, Consumer<E> handler);
    void publish(Object event);
}
