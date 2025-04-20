package model;

import java.time.LocalDate;

import javafx.scene.control.Button;

public class Task {
    private int id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String priority;
    private String category;
    private String status;

    public Task(int id, String title, String description, LocalDate dueDate, String priority, String category, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.category = category;
        this.status = status;
    }

    // Getter and Setter for ID
    public int getId() {
        return id;
    }

    public void setId(int id) { // ✅ Add this method
        this.id = id;
    }

    // Other getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return title + " (" + status + ")";
    }

    public void styleActionButton(Button button, String bgColor, String textColor) {
        button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-size: 14px;" +
            "-fx-min-width: 32px;" +
            "-fx-min-height: 32px;" +
            "-fx-background-radius: 16px;" +
            "-fx-padding: 0;"
        );
        
        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + textColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-min-width: 32px;" +
                "-fx-min-height: 32px;" +
                "-fx-background-radius: 16px;" +
                "-fx-padding: 0;" +
                "-fx-cursor: hand;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: " + textColor + ";" +
                "-fx-font-size: 14px;" +
                "-fx-min-width: 32px;" +
                "-fx-min-height: 32px;" +
                "-fx-background-radius: 16px;" +
                "-fx-padding: 0;"
            );
        });
    }
}
