package com.sfronc.hub.server.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class SimpleEventBus implements EventBus {
    private final Map<Class<?>, List<Consumer<?>>> handlers = new ConcurrentHashMap<>();

    @Override
    public <E> void subscribe(Class<E> type, Consumer<E> handler) {
        handlers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public void publish(Object event) {
        if (event == null) return;
        var list = handlers.get(event.getClass());
        if (list == null) return;

        for (Consumer<?> h: list) {
            @SuppressWarnings("unchecked")
            Consumer<Object> hh = (Consumer<Object>) h;
            hh.accept(event);
        }
    }



}
