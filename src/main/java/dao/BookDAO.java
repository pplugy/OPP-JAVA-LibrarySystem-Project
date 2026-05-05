package dao;

import model.Book;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookDAO {

    private static final Logger LOGGER = Logger.getLogger(BookDAO.class.getName());

    public int addBook(Book book) {
        String sql = "INSERT INTO books (title, author, category, availability_status) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getCategory());
            pstmt.setString(4, book.getAvailabilityStatus());
            pstmt.executeUpdate();

            try (Statement idStmt = conn.createStatement();
                 ResultSet keys = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding book: " + book, e);
        }
        return -1;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY book_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) books.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all books.", e);
        }
        return books;
    }

    public Book getBookById(int id) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching book by ID: " + id, e);
        }
        return null;
    }

    public List<Book> searchBooks(String keyword, String category, String status) {
        List<Book> books = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM books WHERE (LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(category) LIKE ?)");
        if (category != null && !category.isBlank() && !"All".equals(category))
            sql.append(" AND LOWER(category) = ?");
        if (status != null && !status.isBlank() && !"All".equals(status))
            sql.append(" AND LOWER(availability_status) = ?");
        sql.append(" ORDER BY title");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            String like = "%" + (keyword == null ? "" : keyword.toLowerCase()) + "%";
            pstmt.setString(1, like);
            pstmt.setString(2, like);
            pstmt.setString(3, like);

            int idx = 4;
            if (category != null && !category.isBlank() && !"All".equals(category))
                pstmt.setString(idx++, category.toLowerCase());
            if (status != null && !status.isBlank() && !"All".equals(status))
                pstmt.setString(idx, status.toLowerCase());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) books.add(mapRow(rs));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching books.", e);
        }
        return books;
    }

    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title=?, author=?, category=?, availability_status=? WHERE book_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getCategory());
            pstmt.setString(4, book.getAvailabilityStatus());
            pstmt.setInt(5, book.getBookId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating book: " + book, e);
        }
        return false;
    }

    public boolean deleteBook(int id) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting book ID: " + id, e);
        }
        return false;
    }

    public List<String> getDistinctCategories() {
        List<String> cats = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM books ORDER BY category";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) cats.add(rs.getString("category"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching categories.", e);
        }
        return cats;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("book_id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("category"),
            rs.getString("availability_status")
        );
    }
}