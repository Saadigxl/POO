package persistence;

import model.Task;
import java.util.List;

public interface TaskPersistence {
    void addTask(Task task);
    void updateTask(Task task);
    void removeTask(Task task);
    List<Task> getAllTasks();
}

