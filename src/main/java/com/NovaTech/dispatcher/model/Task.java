package com.NovaTech.dispatcher.model;

import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@ToString
public class Task implements Comparable<Task> {
    private final UUID id;
    private final String name;
    private final int priority;
    private final Instant createdTimestamp;
    private final String payload;

    public Task(String name, int priority, String payload) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.priority = priority;
        this.createdTimestamp = Instant.now();
        this.payload = payload;
    }

    // Getters and other methods

    @Override
    public int compareTo(Task other) {
        return Integer.compare(other.priority, this.priority); // Higher priority first
    }
}