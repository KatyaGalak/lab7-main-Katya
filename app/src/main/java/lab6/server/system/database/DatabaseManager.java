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

// import lab6.shared.ticket.Ticket;
// import lab6.shared.ticket.Coordinates;
// import lab6.shared.ticket.Person;
// import lab6.shared.ticket.TicketType;
// import lab6.shared.ticket.Color;

// import java.sql.*;
// import java.util.*;
// //import java.util.concurrent.*;
// //import java.util.concurrent.locks.ReentrantLock;

// public class DatabaseManager {
//     private final String url;
//     private final String user;
//     private final String password;

//     // private final ExecutorService readThreadPool;
//     // private final ReentrantLock collectionLock;

//     // private static DatabaseManager instance;

//     //private Connection connection;

//     public DatabaseManager(String url, String user, String password) {
//         if (!url.startsWith("jdbc:postgresql:")) {
//             throw new IllegalArgumentException("Invalid JDBC URL: must start with jdbc:postgresql:");
//         }
//         this.url = url;
//         this.user = user;
//         this.password = password;
//         /*try {
//             connection = getConnection();
//         } catch (SQLException e) {
//             System.out.println("Error connecting to the database " + e);
//         }*/
        
//         //this.readThreadPool = Executors.newFixedThreadPool(readThreadPoolSize);
//         //this.collectionLock = new ReentrantLock();
//     }

//     private Connection getConnection() throws SQLException {
//         System.out.println("Connecting to: " + url);
//         return DriverManager.getConnection(url, user, password);
//     }

//     public PreparedStatement prepareStatement(String query, Connection connection) throws SQLException {
//         return connection.prepareStatement(query);
//     }

//     public PreparedStatement prepareStatement(String query, Connection connection, Object... params) throws SQLException {
//         PreparedStatement stmt = connection.prepareStatement(query);

//         for (int i = 0; i < params.length; ++i) 
//             stmt.setObject(i + 1, params[i]);

//         return stmt;
//     }

//     public Long getNextId() throws SQLException {
//         try (Connection conn = getConnection();
//             PreparedStatement stmt = conn.prepareStatement("SELECT nextval('ticket_id_seq')")) {
//             ResultSet rs = stmt.executeQuery();
//             if (rs.next()) {
//                 return rs.getLong(1);
//             }
//             throw new SQLException("Failed to retrieve next ID from ticket_id_seq");
//         }
//     }

//     public void write(Collection<Ticket> tickets) throws SQLException {
//         //collectionLock.lock();
//         try (Connection conn = getConnection()) {
//             conn.setAutoCommit(false);
//             try {
//                 // Clear existing data
//                 try (PreparedStatement clearStmt = conn.prepareStatement(
//                         "DELETE FROM ticket; DELETE FROM coordinates; DELETE FROM person")) {
//                     clearStmt.executeUpdate();
//                 }

//                 // Prepare statements
//                 PreparedStatement coordStmt = conn.prepareStatement(
//                         "INSERT INTO coordinates (x, y) VALUES (?, ?) RETURNING id");
//                 PreparedStatement personStmt = conn.prepareStatement(
//                         "INSERT INTO person (birthday, passport_id, hair_color) VALUES (?, ?, ?) RETURNING id");
//                 PreparedStatement ticketStmt = conn.prepareStatement(
//                         "INSERT INTO ticket (id, name, coordinates_id, creation_date, price, refundable, type, person_id, creator_id) " +
//                                 "VALUES (?, ?, ?, ?, ?, ?, ?::ticket_type, ?, ?)");

//                 for (Ticket ticket : tickets) {
//                     // creationDate and id are set in constructor, no setters available

//                     if (ticket.getId() == null)
//                         ticket.setId(getNextId());

//                     // Insert Coordinates
//                     Coordinates coords = ticket.getCoordinates();
//                     coordStmt.setDouble(1, coords.getX());
//                     coordStmt.setFloat(2, coords.getY());
//                     ResultSet coordRs = coordStmt.executeQuery();
//                     if (!coordRs.next()) {
//                         throw new SQLException("Failed to retrieve coordinates ID");
//                     }
//                     int coordId = coordRs.getInt(1);
//                     if (coordId == -1) {
//                         throw new SQLException("Invalid coordinates ID returned");
//                     }

//                     // Insert Person (non-null)
//                     Person person = ticket.getPerson();
//                     Integer personId = null;
//                     if (person != null) {
//                         personStmt.setTimestamp(1, Timestamp.valueOf(person.getBirthday()));

//                         personStmt.setString(2, person.getPassportID());
                        
//                         personStmt.setString(3, person.getHairColor().name());

//                         ResultSet personRs = personStmt.executeQuery();
//                         if (!personRs.next()) {
//                             throw new SQLException("Failed to retrieve person ID");
//                         }
//                         personId = personRs.getInt(1);
//                     } else {
//                         throw new SQLException("Person cannot be null");
//                     }

//                     // Insert Ticket
//                     ticketStmt.setLong(1, ticket.getId());
//                     ticketStmt.setString(2, ticket.getName());
//                     ticketStmt.setInt(3, coordId);
//                     ticketStmt.setTimestamp(4, Timestamp.valueOf(ticket.getCreationDate()));
//                     ticketStmt.setDouble(5, ticket.getPrice());
//                     ticketStmt.setBoolean(6, ticket.getRefundable());
//                     ticketStmt.setString(7, ticket.getType().name());
//                     ticketStmt.setInt(8, personId);
//                     ticketStmt.setLong(9, ticket.getCreatorId());
//                     ticketStmt.executeUpdate();
//                 }

//                 conn.commit();
//             } catch (SQLException e) {
//                 conn.rollback();
//                 throw e;
//             } finally {
//                 conn.setAutoCommit(true);
//             }
//         } finally {
//             //collectionLock.unlock();
//         }
//     }

//     /*public Future<Collection<Ticket>> readAsync() {
//         return readThreadPool.submit(() -> {
//             collectionLock.lock();
//             try {
//                 return read();
//             } finally {
//                 collectionLock.unlock();
//             }
//         });
//     }*/

//     public Collection<Ticket> read() throws SQLException {
//         Collection<Ticket> tickets = new ArrayList<>();

//         try (Connection conn = getConnection()) {
//             String query = "SELECT t.*, c.x, c.y, p.birthday, p.passport_id, p.hair_color " +
//                     "FROM ticket t " +
//                     "JOIN coordinates c ON t.coordinates_id = c.id " +
//                     "LEFT JOIN person p ON t.person_id = p.id";

//             try (PreparedStatement stmt = conn.prepareStatement(query)) {
//                 ResultSet rs = stmt.executeQuery();

//                 while (rs.next()) {
//                     Coordinates coordinates = new Coordinates(
//                             rs.getDouble("x"),
//                             rs.getFloat("y"));

//                     Person person = null;
//                     if (rs.getString("passport_id") != null) {
//                         java.time.LocalDateTime birthday = null;
//                         Timestamp birthdayTs = rs.getTimestamp("birthday");
//                         if (birthdayTs != null) {
//                             birthday = birthdayTs.toLocalDateTime();
//                         }
//                         String passportId = rs.getString("passport_id");
//                         String hairColorStr = rs.getString("hair_color");
//                         Color hairColor = hairColorStr != null ? Color.valueOf(hairColorStr) : null;
//                         person = new Person(
//                                 birthday,
//                                 passportId,
//                                 hairColor);
//                     }

//                     double price = rs.getDouble("price");
//                     boolean refundable = rs.getBoolean("refundable");
//                     String typeStr = rs.getString("type");

//                     Ticket ticket = new Ticket(
//                             rs.getString("name"),
//                             coordinates,
//                             price,
//                             refundable,
//                             typeStr != null ? TicketType.valueOf(typeStr) : null,
//                             person);

//                     tickets.add(ticket);
//                 }
//             }
//         }

//         return tickets;
//     }

//     public Integer getUserId(String name, String password) throws SQLException {
//         if (name == null || password == null) {
//             throw new IllegalArgumentException("Name and password must not be null");
//         }

//         try (Connection conn = getConnection();
//              PreparedStatement stmt = conn.prepareStatement(
//                      "SELECT id FROM users WHERE name = ? AND password = ?")) {
//             stmt.setString(1, name);
//             stmt.setString(2, password);
//             ResultSet rs = stmt.executeQuery();
//             if (rs.next()) {
//                 return rs.getInt("id");
//             }
//             return null;
//         }
//     }

//     public Integer addUser(String name, String password) throws SQLException {
//         if (name == null || password == null) {
//             throw new IllegalArgumentException("Name and password must not be null");
//         }

//         try (Connection conn = getConnection()) {
//             conn.setAutoCommit(false);
//             try {
//                 // Check if user exists with matching name and password
//                 try (PreparedStatement checkStmt = conn.prepareStatement(
//                         "SELECT id FROM users WHERE name = ? AND password = ?")) {
//                     checkStmt.setString(1, name);
//                     checkStmt.setString(2, password);
//                     ResultSet rs = checkStmt.executeQuery();
//                     if (rs.next()) {
//                         conn.commit();
//                         return rs.getInt("id"); // Return existing user's ID
//                     }
//                 }

//                 // Check if name exists with different password
//                 try (PreparedStatement checkNameStmt = conn.prepareStatement(
//                         "SELECT id FROM users WHERE name = ?")) {
//                     checkNameStmt.setString(1, name);
//                     ResultSet rs = checkNameStmt.executeQuery();
//                     if (rs.next()) {
//                         conn.rollback();
//                         throw new IllegalArgumentException("Username exists with a different password");
//                     }
//                 }

//                 // Insert new user
//                 try (PreparedStatement insertStmt = conn.prepareStatement(
//                         "INSERT INTO users (name, password) VALUES (?, ?) RETURNING id")) {
//                     insertStmt.setString(1, name);
//                     insertStmt.setString(2, password);
//                     ResultSet rs = insertStmt.executeQuery();
//                     if (rs.next()) {
//                         Integer userId = rs.getInt("id");
//                         conn.commit();
//                         return userId;
//                     }
//                     throw new SQLException("Failed to retrieve new user ID");
//                 }
//             } catch (SQLException | IllegalArgumentException e) {
//                 conn.rollback();
//                 throw e;
//             } finally {
//                 conn.setAutoCommit(true);
//             }
//         }
//     }

//     /*public void processRequestAsync(Runnable requestProcessor) {
//         // Process request in a new thread
//         new Thread(requestProcessor).start();
//     }

//     public void sendResponseAsync(Runnable responseSender) {
//         // Send response in a new thread
//         new Thread(responseSender).start();
//     }*/

//     /*public void shutdown() {
//         readThreadPool.shutdown();
//     }*/
// }
