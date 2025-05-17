package lab6.server.system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private final String url = "jdbc:postgresql://pg/studs"; // вынести в EnvManager
    private final String user = "s408417";
    private final String password = System.getProperty("PGPASS");

    

    private static DatabaseManager instance;

    private DatabaseManager() {
        if (!url.startsWith("jdbc:postgresql:")) {
            throw new IllegalArgumentException("Invalid JDBC URL: must start with jdbc:postgresql:");
        }
        
    }

    public static synchronized DatabaseManager getInstance() {
        return (instance == null) ? instance = new DatabaseManager() : instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public PreparedStatement prepareStatement(String query, Connection connection) throws SQLException {
        return connection.prepareStatement(query);
    }

    public PreparedStatement prepareStatement(String query, Connection connection, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; ++i) 
            stmt.setObject(i + 1, params[i]);

        return stmt;
    }
}
