package persistence;

import model.Task;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabasePersistence implements TaskPersistence {
    private static final String URL = "jdbc:mysql://localhost:3306/task_manager";
    private static final String USER = "root"; // Change this if needed
    private static final String PASSWORD = "RoHt9wD67"; // Change this if needed

    public DatabasePersistence() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, due_date, priority, category, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setDate(3, Date.valueOf(task.getDueDate()));
            stmt.setString(4, task.getPriority());
            stmt.setString(5, task.getCategory());
            stmt.setString(6, task.getStatus());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTask(Task task) {
        String sql = "UPDATE tasks SET title=?, description=?, due_date=?, priority=?, category=?, status=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setDate(3, Date.valueOf(task.getDueDate()));
            stmt.setString(4, task.getPriority());
            stmt.setString(5, task.getCategory());
            stmt.setString(6, task.getStatus());
            stmt.setInt(7, task.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeTask(Task task) {
        String sql = "DELETE FROM tasks WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, task.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getString("priority"),
                        rs.getString("category"),
                        rs.getString("status")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }
}
