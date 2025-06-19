package com.NovaTech.dispatcher.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Task implements Comparable<Task>{
    private UUID id =  UUID.randomUUID();
    private String name;
    private int priority;
    private int retryCount;
    private Instant createdTimeStamp;
    private String payload;
    private TaskStatus status;

    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.priority, other.priority);
    }
}