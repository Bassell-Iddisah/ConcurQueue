package com.NovaTech.dispatcher.service;

import com.NovaTech.dispatcher.TaskStatus;
import com.NovaTech.dispatcher.repository.TaskRepository;
import com.NovaTech.dispatcher.util.JsonExporter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.*;

@AllArgsConstructor
public class MonitorService implements Runnable {
    private static final long STUCK_THRESHOLD_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long EXPORT_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    private final TaskRepository taskRepository;
    private final ExecutorService executorService;
    private long lastExportTime = System.currentTimeMillis();

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                logQueueStatus();
                checkForStuckTasks();

                // Export to JSON every minute
                if (System.currentTimeMillis() - lastExportTime > EXPORT_INTERVAL_MS) {
                    exportTaskStatus();
                    lastExportTime = System.currentTimeMillis();
                }

                Thread.sleep(5000); // Check every 5 seconds
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void checkForStuckTasks() {
        Map<UUID, Long> processingTasks = taskRepository.getProcessingTasks();
        long currentTime = System.currentTimeMillis();

        processingTasks.forEach((taskId, startTime) -> {
            if (currentTime - startTime > STUCK_THRESHOLD_MS) {
                System.err.printf("Stuck task detected: %s (processing for %d ms)%n",
                        taskId, currentTime - startTime);
                // Optional: Handle stuck task (e.g., mark as failed, retry)
            }
        });
    }

    private void exportTaskStatus() {
        try {
            JsonExporter.exportTaskStatus(taskRepository);
            System.out.println("Task status exported to JSON");
        } catch (IOException e) {
            System.err.println("Failed to export task status: " + e.getMessage());
        }
    }

    private void logQueueStatus() {
        int queueSize = taskRepository.getQueueSize();
        int activeThreads = ((ThreadPoolExecutor) executorService).getActiveCount();
        long completedTasks = taskRepository.getCompletedTaskCount();

        System.out.println();
        System.out.printf(
                "[Monitor] Queue: %d tasks | Active workers: %d | Completed: %d%n",
                queueSize,
                activeThreads,
                completedTasks
        );
        System.out.println();
    }
}