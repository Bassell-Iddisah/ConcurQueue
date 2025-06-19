package com.NovaTech.dispatcher.repository;

import com.NovaTech.dispatcher.model.Task;
import lombok.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SharedTaskQueue {
    private int size = 0;
    private BlockingQueue<Task> TaskQueue = new PriorityBlockingQueue<>();

    // Add a Task to sharedQueue
    public static boolean add(Task task) {
        return false;
    }

    // Remove Task from sharedQueue
    public boolean pop(Task task) {
        return false;
    }
}
