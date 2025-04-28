package model;

import java.util.ArrayList;
import java.util.List;
import model.Task;
public class User {
    private int id;
    private String username;
    private List<Task> tasks;

    public User(int id, String username) {
        this.id = id;
        this.username = username;
        this.tasks = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public List<Task> getTasks() { return tasks; }

    public void addTask(Task task) { tasks.add(task); }
    public void removeTask(Task task) { tasks.remove(task); }
    public void updateTaskStatus(Task task, String status) { task.setStatus(status); }
}
