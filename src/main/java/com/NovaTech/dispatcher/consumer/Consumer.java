package com.NovaTech.dispatcher.consumer;

import com.NovaTech.dispatcher.TaskStatus;
import com.NovaTech.dispatcher.model.Task;
import com.NovaTech.dispatcher.repository.TaskRepository;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer implements Runnable {
    private final TaskRepository taskRepository;
    private final AtomicInteger processedCount;

    public Consumer(TaskRepository taskRepository, AtomicInteger processedCount) {
        this.taskRepository = taskRepository;
        this.processedCount = processedCount;
        System.out.printf("[%s] Consumer created%n", Thread.currentThread().getName());
    }

    @Override
    public void run() {
        try {
            System.out.println();
            System.out.printf("[%s] started%n", Thread.currentThread().getName());

            while (!Thread.currentThread().isInterrupted()) {
                Task task = taskRepository.getNextTask();

                if (task != null) {
                    System.out.printf("[%s][%tT] Processing task: %s (Priority: %d)%n",
                            Thread.currentThread().getName(),
                            new Date(),
                            task.getName(),
                            task.getPriority());

                    processTask(task);
                    processedCount.incrementAndGet();

                    System.out.printf("[%s] Completed processing: %s%n",
                            Thread.currentThread().getName(), task.getName());
                    System.out.println(); // Blank line for separation
                } else {
                    System.out.printf("[%s] No tasks available, waiting...%n",
                            Thread.currentThread().getName());
                    Thread.sleep(1000); // Brief pause when queue is empty
                }
            }
        } catch (InterruptedException e) {
            System.out.printf("[%s] Consumer interrupted%n", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.printf("[%s] Unexpected error: %s%n",
                    Thread.currentThread().getName(), e.getMessage());
        } finally {
            System.out.printf("[%s] Consumer shutting down%n", Thread.currentThread().getName());
        }
    }

    private void processTask(Task task) {
        try {
            String threadName = Thread.currentThread().getName();
            System.out.printf("[%s] START Processing: %s (Priority: %d)%n",
                    threadName, task.getName(), task.getPriority());

            taskRepository.updateTaskStatus(task.getId(), TaskStatus.PROCESSING);

            Thread.sleep(1000 + new Random().nextInt(2000));

            if (new Random().nextInt(10) == 0) {
                throw new RuntimeException("Simulated failure");
            }

            taskRepository.updateTaskStatus(task.getId(), TaskStatus.COMPLETED);
            taskRepository.incrementCompletedTasks();
            System.out.printf("[%s] FINISHED: %s%n", threadName, task.getName());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.printf("[%s] FAILED: %s - %s%n",
                    Thread.currentThread().getName(), task.getName(), e.getMessage());
            taskRepository.updateTaskStatus(task.getId(), TaskStatus.FAILED);
            handleFailedTask(task);
        }
    }

    private void handleFailedTask(Task task) {
        int retryCount = taskRepository.incrementRetryCount(task.getId());

        if (retryCount <= 3) {
            System.out.printf("Retrying task %s (attempt %d of 3)%n",
                    task.getId(), retryCount);
            taskRepository.addTask(task); // Requeue for retry
        } else {
            System.out.printf("Task %s failed after %d attempts%n",
                    task.getId(), retryCount);
        }
    }
}