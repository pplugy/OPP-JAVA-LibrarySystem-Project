package dao;

import model.BorrowRecord;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BorrowDAO {

    private static final Logger LOGGER = Logger.getLogger(BorrowDAO.class.getName());

    private static final String SELECT_WITH_NAMES =
        "SELECT br.*, b.title AS book_title, m.member_name " +
        "FROM borrow_records br " +
        "JOIN books b ON br.book_id = b.book_id " +
        "JOIN members m ON br.member_id = m.member_id ";

    public int addRecord(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (book_id, member_id, borrow_date, due_date, return_status) " +
                     "VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, record.getBookId());
            pstmt.setInt(2, record.getMemberId());
            pstmt.setString(3, record.getBorrowDate().toString());
            pstmt.setString(4, record.getDueDate().toString());
            pstmt.setString(5, record.getReturnStatus());
            pstmt.executeUpdate();

            try (Statement idStmt = conn.createStatement();
                 ResultSet keys = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding borrow record.", e);
        }
        return -1;
    }

    public List<BorrowRecord> getAllRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = SELECT_WITH_NAMES + "ORDER BY br.record_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) records.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all borrow records.", e);
        }
        return records;
    }

    public BorrowRecord getRecordById(int id) {
        String sql = SELECT_WITH_NAMES + "WHERE br.record_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching record by ID: " + id, e);
        }
        return null;
    }

    public List<BorrowRecord> searchRecords(String keyword, String status,
                                            LocalDate fromDate, LocalDate toDate) {
        List<BorrowRecord> records = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_WITH_NAMES);
        sql.append("WHERE (LOWER(b.title) LIKE ? OR LOWER(m.member_name) LIKE ?)");
        if (status != null && !status.isBlank() && !"All".equals(status))
            sql.append(" AND LOWER(br.return_status) = ?");
        if (fromDate != null) sql.append(" AND br.borrow_date >= ?");
        if (toDate != null)   sql.append(" AND br.borrow_date <= ?");
        sql.append(" ORDER BY br.borrow_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            String like = "%" + (keyword == null ? "" : keyword.toLowerCase()) + "%";
            pstmt.setString(1, like);
            pstmt.setString(2, like);

            int idx = 3;
            if (status != null && !status.isBlank() && !"All".equals(status))
                pstmt.setString(idx++, status.toLowerCase());
            if (fromDate != null) pstmt.setString(idx++, fromDate.toString());
            if (toDate != null)   pstmt.setString(idx, toDate.toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) records.add(mapRow(rs));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching borrow records.", e);
        }
        return records;
    }

    public List<BorrowRecord> getOverdueRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = SELECT_WITH_NAMES +
            "WHERE br.return_status != 'Returned' AND br.due_date < ? " +
            "ORDER BY br.due_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) records.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching overdue records.", e);
        }
        return records;
    }

    public boolean updateRecord(BorrowRecord record) {
        String sql = "UPDATE borrow_records SET book_id=?, member_id=?, borrow_date=?, due_date=?, return_status=? " +
                     "WHERE record_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, record.getBookId());
            pstmt.setInt(2, record.getMemberId());
            pstmt.setString(3, record.getBorrowDate().toString());
            pstmt.setString(4, record.getDueDate().toString());
            pstmt.setString(5, record.getReturnStatus());
            pstmt.setInt(6, record.getRecordId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating borrow record.", e);
        }
        return false;
    }

    public boolean deleteRecord(int id) {
        String sql = "DELETE FROM borrow_records WHERE record_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting borrow record ID: " + id, e);
        }
        return false;
    }

    public int autoMarkOverdue() {
        String sql = "UPDATE borrow_records SET return_status='Overdue' " +
                     "WHERE return_status='Borrowed' AND due_date < ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error auto-marking overdue records.", e);
        }
        return 0;
    }

    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord(
            rs.getInt("record_id"),
            rs.getInt("book_id"),
            rs.getInt("member_id"),
            LocalDate.parse(rs.getString("borrow_date")),
            LocalDate.parse(rs.getString("due_date")),
            rs.getString("return_status")
        );
        record.setBookTitle(rs.getString("book_title"));
        record.setMemberName(rs.getString("member_name"));
        return record;
    }
}