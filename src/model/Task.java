package model;

import java.time.LocalDate;

public class Task {
    private String title;
    private String description;
    private LocalDate dueDate;
    private String status; // "À faire", "En cours", "Terminé"
    private String priority; // "Haute", "Moyenne", "Basse"
    private String category;

    public Task(String title, String description, LocalDate dueDate, String priority, String category) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = "À faire";
        this.priority = priority;
        this.category = category;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return title + " (" + status + ")";
    }
}
