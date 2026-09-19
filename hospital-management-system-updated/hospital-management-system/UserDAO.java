import java.sql.*;

/**
 * Handles authentication and user management against the `users` table.
 */
public class UserDAO {

    public User authenticate(String username, String password) throws HospitalException {
        String sql = "SELECT id, username, password_hash, salt, role FROM users WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new HospitalException("Invalid username or password!");
                }
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                if (!PasswordUtil.verifyPassword(password, salt, storedHash)) {
                    throw new HospitalException("Invalid username or password!");
                }
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
            }
        } catch (SQLException e) {
            throw new HospitalException("Database error during login: " + e.getMessage());
        }
    }

    public void registerUser(String username, String password, String role) throws HospitalException {
        if (username == null || username.trim().isEmpty()) {
            throw new HospitalException("Username cannot be empty!");
        }
        if (password == null || password.length() < 4) {
            throw new HospitalException("Password must be at least 4 characters!");
        }
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        String sql = "INSERT INTO users (username, password_hash, salt, role) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, role);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new HospitalException("Username already exists!");
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage());
        }
    }
}
