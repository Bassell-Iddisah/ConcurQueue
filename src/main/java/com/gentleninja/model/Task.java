package com.gentleninja.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class Task implements Comparable<Task>{
    private UUID id;
    private String name;
    private int priority;
    private int retryCount;
    private Instant createdTimeStamp;
    private String payload;
    private boolean processed = false;

    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.priority, other.priority);
    }
}
