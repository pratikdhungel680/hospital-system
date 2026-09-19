import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized JDBC connection manager.
 * Update these constants (or externalize to a config file) to match your MySQL setup.
 */
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add Connector/J to your classpath.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Quick check used by the GUI at startup to give a clear error instead of a crash. */
    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
