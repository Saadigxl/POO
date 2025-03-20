package persistence;

import model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabasePersistence {
    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    public DatabasePersistence() {
        initializeDatabase();
    }

    // Initialize the database and create the tasks table if it doesn't exist
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "dueDate TEXT, " +
                    "status TEXT, " +
                    "priority TEXT, " +
                    "category TEXT)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Save tasks to the database
    public void saveTasks(List<Task> tasks) {
        String sql = "INSERT INTO tasks (title, description, dueDate, status, priority, category) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Task task : tasks) {
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setString(3, task.getDueDate().toString());
                pstmt.setString(4, task.getStatus());
                pstmt.setString(5, task.getPriority());
                pstmt.setString(6, task.getCategory());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load tasks from the database
    public List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Task task = new Task(
                        rs.getString("title"),
                        rs.getString("description"),
                        LocalDate.parse(rs.getString("dueDate")),
                        rs.getString("priority"),
                        rs.getString("category")
                );
                task.setStatus(rs.getString("status"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
}