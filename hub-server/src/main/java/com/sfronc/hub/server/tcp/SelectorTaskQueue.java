package com.sfronc.hub.server.tcp;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

final class SelectorTaskQueue {
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public void submit(Runnable task) {
        tasks.add(task);
    }

    public void drain() {
        for (;;) {
            Runnable task = tasks.poll();
            if (task == null) return;;
            task.run();
        }
    }
}
