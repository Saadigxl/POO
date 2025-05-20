package model;

import java.time.LocalDateTime;

public class Note {
    private int id;
    private String content;
    private LocalDateTime createdAt;
    private int taskId;
    private int userId;

    public Note(int id, String content, LocalDateTime createdAt, int taskId, int userId) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.taskId = taskId;
        this.userId = userId;
    }

    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


}