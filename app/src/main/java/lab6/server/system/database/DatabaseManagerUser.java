package lab6.server.system.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lab6.shared.io.connection.UserCredentials;

public class DatabaseManagerUser {
    private static final String CHECK_PASSWORD_USER = "SELECT password FROM users WHERE name = ?";
    private static final String ADD_USER = "INSERT INTO users (user_id, name, password) VALUES (default, ?, ?)";
    private static final String FIND_UID_BY_NAME = "SELECT user_id FROM users WHERE name = ?";

    private static DatabaseManagerUser instance;

    private DatabaseManagerUser() {}

    public static synchronized DatabaseManagerUser getInstance() {
        return (instance == null) ? instance = new DatabaseManagerUser() : instance;
    }

    public boolean checkPassword(UserCredentials userCredentials) {
        try {
            try (Connection conn = DatabaseManager.getInstance().getConnection()) {
                //conn.setAutoCommit(false);

                try (PreparedStatement stmt = DatabaseManager.getInstance().
                                                                    prepareStatement(CHECK_PASSWORD_USER, conn, userCredentials.username())
                    ) {
                        ResultSet res = stmt.executeQuery();

                        if (res.next())
                            return res.getString("password").equals(userCredentials.passwordHash());

                        return false;
                }
            }
        } catch (SQLException e) {
            //conn.rollback();
            return false;
        }
    }

    public long getUserId(String name) {
        try {
            try (Connection conn = DatabaseManager.getInstance().getConnection()) {
                //conn.setAutoCommit(false);

                try (PreparedStatement stmt = DatabaseManager.getInstance().
                                                                    prepareStatement(FIND_UID_BY_NAME, conn, name)
                    ) {
                        ResultSet res = stmt.executeQuery();

                        if (res.next())
                            return res.getLong("user_id");

                        return -1;
                }
            }
        } catch (SQLException e) {
            //conn.rollback();
            return -1;
        }
    }

    public boolean addUser(UserCredentials userCredentials) {
        try {
            try (Connection conn = DatabaseManager.getInstance().getConnection()) {
                //conn.setAutoCommit(false);

                try (PreparedStatement insertStmt = DatabaseManager.getInstance().
                                                                    prepareStatement(ADD_USER, conn, userCredentials.username(), 
                                                                    userCredentials.passwordHash())
                    ) {
                        insertStmt.executeUpdate();
                        //conn.commit();
                        return true;
                }
            }
        } catch (SQLException e) {

            e.printStackTrace();
            //conn.rollback();
            return false;
        }
        
    }

    // public long getUserID(String login) {
    //     try {
    //         try (Connection conn = DatabaseManager.getInstance().getConnection()) {
    //             conn.setAutoCommit(false);

    //             try (PreparedStatement insertStmt = DatabaseManager.getInstance().
    //                                                                 prepareStatement(ADD_USER, conn, userCredentials.username(), 
    //                                                                 userCredentials.passwordHash())
    //                 ) {
    //                     insertStmt.executeUpdate();
    //                     return true;
    //             }
    //         }
    //     } catch (SQLException e) {
    //         return false;
    //     }
    // }
}
