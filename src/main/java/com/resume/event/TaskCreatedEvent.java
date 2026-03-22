package com.resume.event;

public class TaskCreatedEvent {
    private final Long taskId;

    public TaskCreatedEvent(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }
}

