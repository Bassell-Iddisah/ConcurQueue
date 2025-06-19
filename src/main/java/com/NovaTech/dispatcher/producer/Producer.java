package com.NovaTech.dispatcher.producer;

import com.NovaTech.dispatcher.model.Task;
import com.NovaTech.dispatcher.repository.TaskRepository;
import java.util.Random;

public class Producer implements Runnable {
    private final TaskRepository taskRepository;
    private final String producerName;
    private final Random random = new Random();

    public Producer(String name, TaskRepository taskRepository) {
        this.producerName = name;
        this.taskRepository = taskRepository;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int taskCount = random.nextInt(3) + 1; // 1-3 tasks
                System.out.println();
                for (int i = 0; i < taskCount; i++) {
                    Task task = generateTask();
                    taskRepository.addTask(task);
                    System.out.printf("[%s] produced task: %s%n", producerName, task);
                }
                Thread.sleep(5000); // Wait 5 seconds
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Task generateTask() {
        int priority = random.nextBoolean() ? 1 : 10; // 1=low, 10=high
        return new Task("Task-" + random.nextInt(1000), priority, "Payload");
    }
}