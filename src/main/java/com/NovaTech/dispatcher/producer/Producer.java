package com.NovaTech.producer;

import com.NovaTech.model.Task;
import lombok.AllArgsConstructor;

import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class Producer implements Runnable {
    private int taskNo;
    private BlockingQueue<Task> taskQueue;

    @Override
    public void run() {
        for( int i = 0; i < this.taskNo; i++ ) {

        }
    }
}
