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
public class Task {
    private UUID id;
    private String name;
    private int priority;
    private Instant createdTimeStamp;
    private String payload;
}
