package dao;

import model.Member;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemberDAO {

    private static final Logger LOGGER = Logger.getLogger(MemberDAO.class.getName());

    public int addMember(Member member) {
        String sql = "INSERT INTO members (member_name, email, membership_type) VALUES (?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getMemberName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getMembershipType());
            pstmt.executeUpdate();

            try (Statement idStmt = conn.createStatement();
                 ResultSet keys = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding member: " + member, e);
        }
        return -1;
    }

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY member_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) members.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all members.", e);
        }
        return members;
    }

    public Member getMemberById(int id) {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching member by ID: " + id, e);
        }
        return null;
    }

    public List<Member> searchMembers(String keyword, String membershipType) {
        List<Member> members = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM members WHERE (LOWER(member_name) LIKE ? OR LOWER(email) LIKE ?)");
        if (membershipType != null && !membershipType.isBlank() && !"All".equals(membershipType))
            sql.append(" AND LOWER(membership_type) = ?");
        sql.append(" ORDER BY member_name");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            String like = "%" + (keyword == null ? "" : keyword.toLowerCase()) + "%";
            pstmt.setString(1, like);
            pstmt.setString(2, like);

            if (membershipType != null && !membershipType.isBlank() && !"All".equals(membershipType))
                pstmt.setString(3, membershipType.toLowerCase());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) members.add(mapRow(rs));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching members.", e);
        }
        return members;
    }

    public boolean updateMember(Member member) {
        String sql = "UPDATE members SET member_name=?, email=?, membership_type=? WHERE member_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, member.getMemberName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getMembershipType());
            pstmt.setInt(4, member.getMemberId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating member: " + member, e);
        }
        return false;
    }

    public boolean deleteMember(int id) {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting member ID: " + id, e);
        }
        return false;
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        return new Member(
            rs.getInt("member_id"),
            rs.getString("member_name"),
            rs.getString("email"),
            rs.getString("membership_type")
        );
    }
}