package lab6.server.system.collection;

//import java.io.IOException;
//import java.sql.SQLException;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

//import lab6.server.system.configuration.FileConfiguration;
import lab6.server.system.database.DatabaseManagerTicket;
//import lab6.server.system.database.DatabaseManager;
//import lab6.server.system.file.CSVHandler;
import lab6.shared.ticket.Ticket;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

import lombok.Getter;

/**
 * The CollectionManager class manages a collection of Ticket objects.
 * It provides methods to load and save the collection from/to a CSV file,
 * as well as access the collection as a list.
 */
@Getter
public class CollectionManager {
    
    /**
     * Retrieves the singleton instance of the CollectionManager.
     * 
     * @return the singleton instance of CollectionManager
     */

    private static CollectionManager instance;

    private TreeSet<Ticket> ticketCollection = new TreeSet<>();

    private final ReentrantLock lock = new ReentrantLock(true);

    @Getter
    private LocalDateTime creationDate;

    public static synchronized CollectionManager getInstance() { 
        return instance == null ? instance = new CollectionManager() : instance;
    }

    //private DatabaseManager db = new DatabaseManager("jdbc:postgresql://pg/studs",
           // "s408417",
           // System.getProperty("PGPASS"));

    private CollectionManager() {
        loadCollection();
        creationDate = LocalDateTime.now();
    }

    /**
     * Sets the ticket collection to the specified TreeSet of Tickets.
     * 
     * @param collection the TreeSet of Tickets to set
     */
    public void setCollection(TreeSet<Ticket> collection) {
        lock.lock();
        try {
            this.ticketCollection = collection;
        } finally {
            lock.unlock();
        }
    }

    public boolean deleteByID(long userID, long tiketID) {
        lock.lock();
        try {
            if (DatabaseManagerTicket.getInstance().deleteByID(userID, tiketID))
                return true;

            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean deleteByUser(long userID) {
        lock.lock();
        try {
            if (!DatabaseManagerTicket.getInstance().deleteByUser(userID))
                return false;
        } finally {
            lock.unlock();
        }

        return true;
    }

    public boolean updateTicketByID(Ticket oldTicket, Ticket newTicket) {
        lock.lock();
        try {
            if (!DatabaseManagerTicket.getInstance().updateByID(newTicket, oldTicket.getId())) {
                return false;
            }
            ticketCollection.remove(oldTicket);
            ticketCollection.add(newTicket);

            return true;
        } finally {
            lock.unlock();
        }
        
    }

    public boolean add(Ticket ticket) {
        lock.lock();

        try {
            long ticketID = DatabaseManagerTicket.getInstance().add(ticket);

            if (ticketID != -1) {
                ticket.setId(ticketID);
                ticketCollection.add(ticket);
                return true;
            }
    
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Loads the collection of Tickets from a CSV file.
     * Clears the existing collection before loading.
     */
    public void loadCollection() {

        lock.lock();

        try {
            ticketCollection.clear();
            ticketCollection.addAll(DatabaseManagerTicket.getInstance().getCollectionFromDB());
        } finally {
            lock.unlock();
        }
        
        
        // try (CSVHandler cvsHandler = new CSVHandler(FileConfiguration.FILE_PATH, true)) {
        //     ticketCollection.addAll(cvsHandler.read());
        // } catch (IOException e) {
        //     System.err.println("[Error] Error loading the collection. " + e.getMessage());
        // }
    }

    /**
     * Saves the current collection of Tickets to a CSV file.
     */
    public void saveCollection() {
        
        // try (CSVHandler cvsHandler = new CSVHandler(FileConfiguration.FILE_PATH, false)) {
        //     cvsHandler.write(ticketCollection);
        // } catch (IOException e) {
        //     System.err.println("[Error] Error saving the collection. " + e.getMessage());
        // }

    }

    /**
     * Returns a list representation of the current ticket collection.
     * 
     * @return a List of Tickets
     */
    public List<Ticket> getList() {

        lock.lock();

        try {
            return new ArrayList<>(ticketCollection);
        } finally {
            lock.unlock();
        }
    }
}
