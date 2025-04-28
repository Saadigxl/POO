package model;

import java.time.LocalDate;

public class Task {
    private int id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String priority;
    private String category;
    private String status;
    private int userId;

    public Task(int id, String title, String description, LocalDate dueDate, String priority, String category, String status, int userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.category = category;
        this.status = status;
        this.userId = userId;
    }

    // Getters and setters for all fields...
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public int getUserId() { return userId; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setCategory(String category) { this.category = category; }
    public void setStatus(String status) { this.status = status; }
    public void setUserId(int userId) { this.userId = userId; }
}
