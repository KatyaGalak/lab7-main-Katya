package lab6.server.system.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.time.LocalDateTime;

import lab6.shared.ticket.Ticket;
import lab6.shared.ticket.TicketType;
import lab6.shared.ticket.Color;
import lab6.shared.ticket.Coordinates;
import lab6.shared.ticket.Person;

public class DatabaseManagerTicket {
    private static final String ADD_TICKET = "INSERT INTO ticket (ticket_id, name, coordinates_id, creationDate, "
                        + "price, refundable, ticketType, personTicket_id, creator_id)"
                        + "VALUES (default, ?, ?, default, ?, ?, ?::ticket_type, ?, ?) RETURNING ticket_id";

    private static final String ADD_COORDINATES = "INSERT INTO coordinates (coordinates_id, crd_x, crd_y)"
                                        + "VALUES (default, ?, ?) RETURNING coordinates_id";

    private static final String ADD_PERSON = "INSERT INTO personTicket (personTicket_id, birthday, passport_id, hairColor)"
                                        + "VALUES (default, ?, ?, ?::hair_color) RETURNING personTicket_id";

    private static final String DELETE_TICKETS_BY_USER = "DELETE FROM ticket WHERE creator_id = ?";

    private static final String GET_PASSPORT_ID_TICKETS_BY_ID = "SELECT pt.passport_id FROM personTicket pt JOIN ticket t ON " 
                                        + "pt.personTicket_id = t.personTicket_id WHERE t.creator_id = ?";

    private static final String CHECK_USER_FOR_TICKET = "SELECT EXISTS (SELECT 1 FROM ticket WHERE ticket_id = ? AND creator_id = ?) AS is_owner";

    private static final String DELETE_BY_ID = "DELETE FROM ticket WHERE ticket_id = ?";

    private static final String ADD_TICKET_WITH_ID = "INSERT INTO ticket (ticket_id, name, coordinates_id, creationDate, "
                        + "price, refundable, ticketType, personTicket_id, creator_id)"
                        + "VALUES (?, ?, ?, default, ?, ?, ?::ticket_type, ?, ?)";

    private static final String GET_ALL_TICKETS = "SELECT t.ticket_id, t.name AS ticket_name, t.creationDate, " +
                                    "t.price, t.refundable, t.ticketType, t.creator_id, " +
                                    "c.coordinates_id, c.crd_x, c.crd_y, " +
                                    "pt.personTicket_id, pt.birthday, pt.passport_id, pt.hairColor " +
                                    "FROM ticket t " +
                                    "INNER JOIN coordinates c ON t.coordinates_id = c.coordinates_id " +
                                    "INNER JOIN personTicket pt ON t.personTicket_id = pt.personTicket_id " +
                                    "ORDER BY t.ticket_id";

    private static DatabaseManagerTicket instance;

    private static final Logger logger = Logger.getLogger(DatabaseManagerTicket.class.getName());

    private DatabaseManagerTicket() {}

    public static synchronized DatabaseManagerTicket getInstance() {
        return (instance == null) ? instance = new DatabaseManagerTicket() : instance;
    }

    public List<Ticket> getCollectionFromDB() {
        List<Ticket> ans = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {

            try {
                try (PreparedStatement stmt = conn.prepareStatement(GET_ALL_TICKETS)) {
                    try (ResultSet res = stmt.executeQuery()) {
                        while (res.next()) {
                            Long ticketID = res.getLong("ticket_id");
                            String ticketName = res.getString("ticket_name");
                            LocalDateTime creationDate = res.getTimestamp("creationDate").toLocalDateTime();
                            
                            Double price = res.getObject("price", Double.class);
                            Boolean refundable = res.getObject("refundable", Boolean.class);

                            TicketType type = TicketType.valueOf(res.getString("ticketType"));

                            Double crdX = res.getDouble("crd_x");
                            Float crdY = res.getFloat("crd_y");
                            Coordinates crd = new Coordinates(crdX, crdY);

                            LocalDateTime birthday = res.getTimestamp("birthday").toLocalDateTime();
                            String passportID = res.getString("passport_id");
                            Color hairColor = Color.valueOf(res.getString("hairColor"));
                            Person person = new Person(birthday, passportID, hairColor);

                            Long creatorID = res.getLong("creator_id");

                            Ticket newTicket = new Ticket(ticketName, crd, price, refundable, type, person, creationDate);
                            newTicket.setId(ticketID);
                            newTicket.setCreatorId(creatorID);

                            ans.add(newTicket);
                        }
                            
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ans;
    }

    private int addPerson(Ticket ticket, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(ADD_PERSON)) {
            stmt.setObject(1, ticket.getPerson().getBirthday());
            stmt.setString(2, ticket.getPerson().getPassportID());
            stmt.setString(3, ticket.getPerson().getHairColor().name());
            ResultSet res = stmt.executeQuery();

            if (!res.next())
                throw new SQLException("Error when creating the person.");

            return res.getInt(1);
        }
    }

    private int addCoordinates(Ticket ticket, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(ADD_COORDINATES)) {
            stmt.setDouble(1, ticket.getCoordinates().getX());
            stmt.setFloat(2, ticket.getCoordinates().getY());
            ResultSet res = stmt.executeQuery();

            if (!res.next())
                throw new SQLException("Error when creating the coordinates.");

            return res.getInt(1);
        }
    }

    public boolean updateByID(Ticket ticket, long ticketID) {
        deleteByID(ticket.getCreatorId(), ticketID);

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            int coordinatesID;
            int personID;

            try {
                
                coordinatesID = addCoordinates(ticket, conn);
                personID = addPerson(ticket, conn);

                try (PreparedStatement stmt = conn.prepareStatement(ADD_TICKET_WITH_ID)) {
                    stmt.setLong(1, ticketID);
                    stmt.setString(2, ticket.getName());
                    stmt.setInt(3, coordinatesID);

                    stmt.setObject(4, ticket.getInstalledPrice() ? ticket.getPrice() : null, java.sql.Types.DOUBLE);
                    stmt.setObject(5, ticket.getInstalledRefundable() ? ticket.getRefundable() : null, java.sql.Types.BOOLEAN);
                    stmt.setString(6, ticket.getType().name());
                    stmt.setInt(7, personID);
                    stmt.setLong(8, ticket.getCreatorId());

                    stmt.executeUpdate();
                }
                conn.commit();

                return true;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteByID(long userID, long ticketID) {
        if (!checkIsOwnerForTicket(userID, ticketID)) 
            return false;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {
                    stmt.setLong(1, ticketID);
                    stmt.executeUpdate();
                }
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean checkIsOwnerForTicket(long userID, long ticketID) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {

            try {
                try (PreparedStatement stmt = conn.prepareStatement(CHECK_USER_FOR_TICKET)) {
                    stmt.setLong(1, ticketID);
                    stmt.setLong(2, userID);
                    
                    try (ResultSet res = stmt.executeQuery()) {
                        if (res.next()) {
                            return res.getBoolean("is_owner");
                        }
                            
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    public List<String> getPassportIDTicketsByUser(long userID) {
        List<String> ans = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {

            try {
                try (PreparedStatement stmt = conn.prepareStatement(GET_PASSPORT_ID_TICKETS_BY_ID)) {
                    stmt.setLong(1, userID);
                    
                    try (ResultSet res = stmt.executeQuery()) {
                        while (res.next()) 
                            ans.add(res.getString("passport_id"));
                    }
                }

                

            } catch (SQLException e) {
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ans;
    }

    public boolean deleteByUser(long userID) {
        logger.info("[DELETE BY USER (DATABASE)] " + userID);
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt = conn.prepareStatement(DELETE_TICKETS_BY_USER)) {
                    stmt.setLong(1, userID);
                    stmt.executeUpdate();
                }
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public long add(Ticket ticket) {
        long ticketID = -1;
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            int coordinatesID;
            int personID;

            try {
                coordinatesID = addCoordinates(ticket, conn);
                personID = addPerson(ticket, conn);

                try (PreparedStatement stmt = conn.prepareStatement(ADD_TICKET)) {
                    stmt.setString(1, ticket.getName());
                    stmt.setInt(2, coordinatesID);

                    stmt.setObject(3, ticket.getInstalledPrice() ? ticket.getPrice() : null, java.sql.Types.DOUBLE);
                    stmt.setObject(4, ticket.getInstalledRefundable() ? ticket.getRefundable() : null, java.sql.Types.BOOLEAN);
                    stmt.setString(5, ticket.getType().name());
                    stmt.setInt(6, personID);
                    stmt.setLong(7, ticket.getCreatorId());

                    ResultSet res = stmt.executeQuery();
    
                    if (!res.next())
                        throw new SQLException("Error when creating the ticket.");
    
                    ticketID = res.getInt(1);
                }
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }

            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ticketID;
    }
}

