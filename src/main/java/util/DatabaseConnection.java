package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static Connection instance = null;

    private DatabaseConnection() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                instance = DriverManager.getConnection(DB_URL);
                instance.setAutoCommit(true);
                LOGGER.info("Database connection established.");
                initialiseSchema(instance);
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "SQLite JDBC driver not found.", e);
                throw new SQLException("SQLite JDBC driver not found.", e);
            }
        }
        return instance;
    }
// SQL queries 
    private static void initialiseSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS books (" +
                "  book_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  title TEXT NOT NULL," +
                "  author TEXT NOT NULL," +
                "  category TEXT NOT NULL," +
                "  availability_status TEXT NOT NULL" +
                ");"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS members (" +
                "  member_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  member_name TEXT NOT NULL," +
                "  email TEXT NOT NULL UNIQUE," +
                "  membership_type TEXT NOT NULL" +
                ");"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS borrow_records (" +
                "  record_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  book_id INTEGER NOT NULL," +
                "  member_id INTEGER NOT NULL," +
                "  borrow_date DATE NOT NULL," +
                "  due_date DATE NOT NULL," +
                "  return_status TEXT NOT NULL," +
                "  FOREIGN KEY(book_id) REFERENCES books(book_id)," +
                "  FOREIGN KEY(member_id) REFERENCES members(member_id)" +
                ");"
            );
            seedSampleData(stmt);
        }
    }
 // SQL queries for inserting data into the database 
    private static void seedSampleData(Statement stmt) throws SQLException {
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM books;");
        if (rs.getInt(1) == 0) {
            stmt.executeUpdate("INSERT INTO books (title, author, category, availability_status) VALUES " +
                "('Introduction to Java', 'John Smith', 'Programming', 'Available')," +
                "('Database Systems', 'Maria Garcia', 'Computer Science', 'Borrowed')," +
                "('Software Engineering Principles', 'Alan Brown', 'Engineering', 'Available');");

            stmt.executeUpdate("INSERT INTO members (member_name, email, membership_type) VALUES " +
                "('Alice Johnson', 'alice.johnson@stmarys.ac.uk', 'Student')," +
                "('Michael Lee', 'michael.lee@stmarys.ac.uk', 'Staff')," +
                "('Sara Ahmed', 'sara.ahmed@stmarys.ac.uk', 'Student');");

            stmt.executeUpdate("INSERT INTO borrow_records (book_id, member_id, borrow_date, due_date, return_status) VALUES " +
                "(2, 1, '2025-03-01', '2025-03-15', 'Borrowed')," +
                "(1, 2, '2025-03-02', '2025-03-16', 'Returned')," +
                "(3, 3, '2025-03-05', '2025-03-19', 'Borrowed');");

            LOGGER.info("Sample data seeded.");
        }
    }

    public static synchronized void closeConnection() {
        if (instance != null) {
            try {
                instance.close();
                LOGGER.info("Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection.", e);
            }
        }
    }
}
    

