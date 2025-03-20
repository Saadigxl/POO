package controller;

import javafx.collections.ObservableList;
import model.Task;
import model.User;
import persistence.DatabasePersistence;

import java.time.LocalDate;
import java.util.List;

public class TaskController {
    private User user;
    private DatabasePersistence dataPersistence;

    public TaskController(User user, DatabasePersistence dataPersistence) {
        this.user = user;
        this.dataPersistence = dataPersistence;
    }

    // Add a new task
    public void addTask(String title, String description, LocalDate dueDate, String priority, String category) {
        Task task = new Task(title, description, dueDate, priority, category);
        user.addTask(task);
        dataPersistence.saveTasks(user.getTasks());
    }

    // Update task status
    public void updateTaskStatus(Task task, String status) {
        user.updateTaskStatus(task, status);
        dataPersistence.saveTasks(user.getTasks());
    }

    // Remove a task
    public void removeTask(Task task) {
        user.removeTask(task);
        dataPersistence.saveTasks(user.getTasks());
    }

    // Get all tasks
    public List<Task> getAllTasks() {
        return user.getTasks();
    }

    // Save tasks to the database
    public void saveTasks(ObservableList<Task> tasks) {
        dataPersistence.saveTasks(tasks);
    }
}
