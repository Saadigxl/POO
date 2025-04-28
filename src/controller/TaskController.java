package controller;

import model.Task;
import model.User;
import persistence.TaskPersistence;
import java.util.List;

public class TaskController {
    private User user;
    private TaskPersistence persistence;

    public TaskController(User user, TaskPersistence persistence) {
        this.user = user;
        this.persistence = persistence;
    }

    public List<Task> getAllTasks(int userId) {
        return persistence.getAllTasks(userId);
    }

    public void addTask(Task task) {
        task.setUserId(user.getId());
        persistence.addTask(task);
    }

    public void updateTask(Task task) {
        persistence.updateTask(task);
    }

    public void deleteTask(Task task) {
        persistence.deleteTask(task);
    }
}
