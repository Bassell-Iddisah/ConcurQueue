package com.NovaTech.dispatcher.util;

import com.NovaTech.dispatcher.TaskStatus;
import com.NovaTech.dispatcher.model.Task;
import com.NovaTech.dispatcher.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonExporter {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void exportTaskStatus(TaskRepository taskRepository) throws IOException {
        List<TaskStatusInfo> statusInfoList = new ArrayList<>();

        for (Map.Entry<UUID, TaskStatus> entry : taskRepository.getAllTaskStatuses().entrySet()) {
            TaskStatusInfo info = new TaskStatusInfo();
            info.taskId = entry.getKey();
            info.status = entry.getValue();

            Task task = taskRepository.getTaskById(entry.getKey());
            if (task != null) {
                info.taskName = task.getName();
                info.priority = task.getPriority();
            }

            statusInfoList.add(info);
        }

        // Write to file
        mapper.writeValue(new File("task_status_export.json"), statusInfoList);
    }

    private static class TaskStatusInfo {
        public UUID taskId;
        public String taskName;
        public int priority;
        public TaskStatus status;
    }
}