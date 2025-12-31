package com.warehouse.service;

import com.warehouse.model.Task;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue {
    private final LinkedBlockingQueue<Task> queue;

    public TaskQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public void enqueueTask(Task task) {
        queue.offer(task);
    }

    public Task dequeueTask() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
