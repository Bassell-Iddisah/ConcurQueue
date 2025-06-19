package com.NovaTech.dispatcher.repository;

import com.NovaTech.dispatcher.TaskStatus;
import com.NovaTech.dispatcher.model.Task;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class TaskRepository {
    private final PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>();
    private final Map<UUID, TaskStatus> taskStatusMap = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> retryCountMap = new ConcurrentHashMap<>();
    private final Map<UUID, Long> processingStartTimeMap = new ConcurrentHashMap<>();
    private final AtomicLong completedTasks = new AtomicLong(0);


    public void addTask(Task task) {
        taskQueue.put(task);
        taskStatusMap.put(task.getId(), TaskStatus.SUBMITTED);
        retryCountMap.putIfAbsent(task.getId(), 0);
    }

    public Task getNextTask() {
        return taskQueue.poll();
    }

    public void updateTaskStatus(UUID taskId, TaskStatus status) {
        taskStatusMap.put(taskId, status);
        if (status == TaskStatus.PROCESSING) {
            processingStartTimeMap.put(taskId, System.currentTimeMillis());
        } else if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
            taskQueue.removeIf(task -> task.getId().equals(taskId)); // Remove from queue
        } else {
            processingStartTimeMap.remove(taskId);
        }
    }

    public int incrementRetryCount(UUID taskId) {
        return retryCountMap.compute(taskId, (k, v) -> v == null ? 1 : v + 1);
    }

    public Map<UUID, Long> getProcessingTasks() {
        return new ConcurrentHashMap<>(processingStartTimeMap);
    }

    public TaskStatus getTaskStatus(UUID taskId) {
        return taskStatusMap.getOrDefault(taskId, null);
    }

    public int getQueueSize() {
        return taskQueue.size();
    }

    public Map<UUID, TaskStatus> getAllTaskStatuses() {
        return new ConcurrentHashMap<>(taskStatusMap);
    }

    public Task getTaskById(UUID taskId) {
        // Note: This is inefficient for large queues - consider maintaining a separate map
        return taskQueue.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElse(null);
    }

    public void incrementCompletedTasks() {
        completedTasks.incrementAndGet();
    }

    public long getCompletedTaskCount() {
        return completedTasks.get();
    }
}