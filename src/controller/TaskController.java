package controller;

import model.Task;
import model.User;
import persistence.TaskPersistence;
import java.time.LocalDate;
import java.util.List;

public class TaskController {
    private final User user;
    private final TaskPersistence taskPersistence;

    public TaskController(User user, TaskPersistence taskPersistence) {
        this.user = user;
        this.taskPersistence = taskPersistence;
    }

    /**
     * Adds a new task and ensures it gets stored in the database.
     */
    public void addTask(String title, String description, LocalDate dueDate, String priority, String category, String status) {
        Task task = new Task(0, title, description, dueDate, priority, category, status);
        taskPersistence.addTask(task);
    }

    /**
     * Updates an existing task in the database.
     */
    public void updateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        taskPersistence.updateTask(task);
    }

    /**
     * Removes a task from the database.
     */
    public void removeTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        taskPersistence.removeTask(task);
    }

    /**
     * Retrieves all tasks for the user.
     */
    public List<Task> getAllTasks() {
        return taskPersistence.getAllTasks();
    }
}
