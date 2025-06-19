package com.NovaTech.dispatcher.producer;

import com.NovaTech.dispatcher.model.Task;
import com.NovaTech.dispatcher.repository.SharedTaskQueue;
import lombok.*;

@AllArgsConstructor
class Producer implements Runnable{
    @Override
    public void run() {
        for(int k=1; k<5; k++) {
            for (int i = 1; i < SharedTaskQueue.getSize(); i++) {
                //log here
                System.out.println(Thread.currentThread().getName() + ": creating task " + i);
                Task task = Task.builder()
                        .build();
                try {
                    SharedTaskQueue.add(task);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            }
            //delay thread
            try {
                Thread.sleep(500);
                System.out.println();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
