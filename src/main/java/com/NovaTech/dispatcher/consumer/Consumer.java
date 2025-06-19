package com.NovaTech.dispatcher.consumer;

import com.NovaTech.dispatcher.task.Task;
import com.NovaTech.dispatcher.task.TaskStatus;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;

public class Consumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ExecutorService consumerPool;
    private final BlockingQueue<Task> taskQueue;
    private final Map<UUID, TaskStatus> taskStatusMap;
    private final AtomicInteger tasksProcessedCount = new AtomicInteger(0);
    private final AtomicInteger tasksFailedCount = new AtomicInteger(0);
    private volatile boolean running = true;
    private final int maxRetries;
    private final long fixedProcessingTimeMs;
    private final Lock deadlockDetectionLock = new ReentrantLock();
    private Instant lastExportTime = Instant.now();

    public Consumer(BlockingQueue<Task> taskQueue,
                    Map<UUID, TaskStatus> taskStatusMap,
                    int maxRetries,
                    long fixedProcessingTimeMs) {
        this.taskQueue = taskQueue;
        this.taskStatusMap = taskStatusMap;
        this.consumerPool = Executors.newFixedThreadPool(5); // Fixed pool size as requested
        this.maxRetries = maxRetries;
        this.fixedProcessingTimeMs = fixedProcessingTimeMs;
    }

    @Override
    public void run() {
        logger.info("Consumer started with {} worker threads", 5);
        while (running) {
            try {
                monitorAndExportMetrics();
                detectDeadlocks();

                Task task = taskQueue.poll(100, TimeUnit.MILLISECONDS); // Non-blocking with timeout
                if (task != null) {
                    consumerPool.execute(() -> processTask(task));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                shutdown();
            }
        }
    }

    private void processTask(Task task) {
        try {
            updateTaskStatus(task, TaskStatus.PROCESSING);
            logger.info("Processing task {} (Priority: {})", task.getId(), task.getPriority());

            // Simulate fixed processing time
            Thread.sleep(fixedProcessingTimeMs);

            updateTaskStatus(task, TaskStatus.COMPLETED);
            tasksProcessedCount.incrementAndGet();
            logger.info("Completed task {}", task.getId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleTaskFailure(task, "Processing interrupted");
        } catch (Exception e) {
            handleTaskFailure(task, "Processing failed: " + e.getMessage());
        }
    }

    private void handleTaskFailure(Task task, String errorMessage) {
        task.setRetryCount(task.getRetryCount() + 1);
        logger.error("{} - Task: {}, Retry: {}/{}",
                errorMessage, task.getId(), task.getRetryCount(), maxRetries);

        if (task.getRetryCount() <= maxRetries) {
            taskQueue.offer(task);
            logger.warn("Re-queued task {} for retry", task.getId());
        } else {
            updateTaskStatus(task, TaskStatus.FAILED);
            tasksFailedCount.incrementAndGet();
            logger.error("Permanent failure for task {}", task.getId());
        }
    }

    private void updateTaskStatus(Task task, TaskStatus status) {
        task.setStatus(status);
        taskStatusMap.put(task.getId(), status);
    }

    private void monitorAndExportMetrics() {
        Instant now = Instant.now();
        if (now.isAfter(lastExportTime.plusSeconds(60))) {
            exportMetricsToJson();
            lastExportTime = now;
        }
    }

    private void exportMetricsToJson() {
        try {
            Map<String, Object> metrics = new LinkedHashMap<>();
            metrics.put("timestamp", Instant.now().toString());
            metrics.put("processedTasks", tasksProcessedCount.get());
            metrics.put("failedTasks", tasksFailedCount.get());
            metrics.put("pendingTasks", taskQueue.size());
            metrics.put("activeThreads", ((ThreadPoolExecutor)consumerPool).getActiveCount());

            objectMapper.writeValue(
                    Paths.get("metrics_" + System.currentTimeMillis() + ".json").toFile(),
                    metrics
            );
            logger.info("Exported metrics to JSON");
        } catch (Exception e) {
            logger.error("Failed to export metrics", e);
        }
    }

    private void detectDeadlocks() {
        if (!deadlockDetectionLock.tryLock()) {
            logger.warn("Potential deadlock detected - failed to acquire monitoring lock");
            // Basic deadlock resolution - interrupt blocked threads
            consumerPool.shutdownNow();
            consumerPool = Executors.newFixedThreadPool(5);
            logger.warn("Restarted consumer pool due to deadlock");
        } else {
            try {
                // Lock acquired - no deadlock
                Thread.sleep(10); // Minimal hold time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                deadlockDetectionLock.unlock();
            }
        }
    }

    public void shutdown() {
        running = false;
        logger.info("Shutting down consumer...");

        // Drain remaining tasks
        List<Task> remainingTasks = new ArrayList<>();
        taskQueue.drainTo(remainingTasks);
        logger.info("Drained {} remaining tasks", remainingTasks.size());

        // Final metrics export
        exportMetricsToJson();

        // Shutdown pool
        consumerPool.shutdown();
        try {
            if (!consumerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                consumerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            consumerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Consumer shutdown complete");
    }

    public int getTasksProcessedCount() {
        return tasksProcessedCount.get();
    }
}