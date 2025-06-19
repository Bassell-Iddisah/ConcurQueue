package com.NovaTech.dispatcher.repository;

import com.NovaTech.dispatcher.task.Task;
import com.NovaTech.dispatcher.task.TaskStatus;
import lombok.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class SharedTaskQueue {

    private static BlockingQueue<Task> TaskQueue = new LinkedBlockingQueue<>(10);

    public static String add(Task task) {
        TaskQueue.add(task);
        task.setStatus(TaskStatus.SUBMITTED);
        return task.toString();
    }

    public static List<Task> getAll() {
        return TaskQueue.stream()
                .map(Task -> Task)
                .collect(Collectors.toList());
    }

    public static Task getTaskById(String id) {
        return TaskQueue.peek();
    }

    public String pop(Task task) {
        TaskQueue.remove(task);
        return String.format("Removed task %s", task.getName());
    }

    public static int getSize() {
        return TaskQueue.size();
    }
}
