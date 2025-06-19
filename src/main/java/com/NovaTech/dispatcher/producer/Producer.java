package com.NovaTech.dispatcher.producer;

import com.NovaTech.dispatcher.task.Task;
import com.NovaTech.dispatcher.task.TaskStatus;
import com.NovaTech.dispatcher.repository.SharedTaskQueue;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
public class Producer implements Runnable {
    private int timeCounter = 3;

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        for (int i = 0; i < timeCounter; i++) {
            Task currentTask = Task.builder()
                    .id(UUID.randomUUID())
                    .name("Task" + i)
                    .priority((i + timeCounter) % timeCounter)
                    .status(TaskStatus.SUBMITTED)
                    .createdTimeStamp(Instant.now())
                    .build();

            try {
                SharedTaskQueue.add(currentTask);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
