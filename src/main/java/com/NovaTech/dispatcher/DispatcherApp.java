package com.NovaTech.dispatcher;

import com.NovaTech.dispatcher.consumer.Consumer;
import com.NovaTech.dispatcher.producer.Producer;
import com.NovaTech.dispatcher.repository.TaskRepository;
import com.NovaTech.dispatcher.service.MonitorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DispatcherApp {
    public static void main(String[] args) {
        TaskRepository taskRepository = new TaskRepository();
        AtomicInteger processedCount = new AtomicInteger(0);
        final AtomicInteger consumerCounter = new AtomicInteger(0);

        System.out.println("Starting DispatcherApp with 5 consumers and 3 producers...");


        ExecutorService producerExecutor = Executors.newFixedThreadPool(3);
        for (int i = 1; i <= 3; i++) {
            producerExecutor.execute(new Producer("Producer-" + i, taskRepository));
        }

        ExecutorService consumerExecutor = Executors.newFixedThreadPool(5, r -> {
            Thread t = new Thread(r);
            t.setName("Consumer-" + consumerCounter.incrementAndGet());
            return t;
        });

        for (int i = 0; i < 5; i++) {
            consumerExecutor.execute(new Consumer(taskRepository, processedCount));
        }

        ExecutorService monitorExecutor = Executors.newSingleThreadExecutor();
        monitorExecutor.execute(new MonitorService(taskRepository, consumerExecutor, System.currentTimeMillis()));

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown(producerExecutor, "Producers");
            shutdown(consumerExecutor, "Consumers");
            shutdown(monitorExecutor, "Monitor");
            System.out.println("Total tasks processed: " + processedCount.get());
        }));
    }

    private static void shutdown(ExecutorService executor, String name) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            System.out.println(name + " shutdown complete");
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}