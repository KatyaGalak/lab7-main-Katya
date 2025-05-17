// package lab6.shared.ticket;

// import java.io.IOException;
// import java.util.concurrent.atomic.AtomicLong;

// import lab6.server.system.configuration.FileConfiguration;
// import lab6.server.system.file.FileHandler;

// import java.util.HashSet;

// /**
//  * The IDGenerator class is responsible for generating unique IDs for tickets.
//  * It ensures that each ID is unique and manages the current ID state.
//  */
// public class IDGenerator implements AutoCloseable {

//     private static IDGenerator idGenerator = null;
//     private final FileHandler fileHandler;
//     private final AtomicLong curId = new AtomicLong(0);

//     private static final HashSet<Long> idTickets = new HashSet<>();

        
//     /**
//      * Retrieves the singleton instance of the IDGenerator.
//      * 
//      * @return the singleton instance of IDGenerator
//      * @throws IOException if an I/O error occurs
//      */
//     public static synchronized IDGenerator getInstance() throws IOException {
//         if (idGenerator == null) {
//             idGenerator = new IDGenerator();
//         }

//         return idGenerator;
//     }

//     /**
//      * Initializes the IDGenerator by reading the last used ID from the file.
//      * If the file is empty or contains an invalid number, it resets the ID to 0.
//      */
//     private void initialize() {

//         try {
//             String preId = fileHandler.readLastString();
//             if (preId != null) 
//                 curId.set(Integer.parseInt(preId));
//         } catch (NumberFormatException e) {
//             System.out.println("NumberFormat");
//             fileHandler.write(Long.toString(curId.getAndSet(0)));
//         }
//     }

//     /**
//      * Constructs an IDGenerator object and initializes the file handler.
//      * 
//      * @throws IOException if an I/O error occurs
//      */
//     private IDGenerator() throws IOException {

//         fileHandler = new FileHandler(FileConfiguration.ID_SEQ_PATH, true);
//         initialize();
//     }

//     /**
//      * Generates a new unique ID, writes it to the file, and returns it.
//      * 
//      * @return the newly generated ID
//      * @throws IOException if an I/O error occurs
//      */
//     public long generateId() throws IOException {

//         long newId = curId.incrementAndGet();
//         fileHandler.write(newId + System.lineSeparator());

//         addId(newId);
//         return newId;
//     }

//     /**
//      * Checks if the given ID is unique.
//      * 
//      * @param id the ID to check
//      * @return true if the ID is unique, false otherwise
//      */
//     public boolean isUnique(long id) {

//         return !idTickets.contains(id);
//     }

//     /**
//      * Deletes the last generated ID from the set of unique IDs.
//      */
//     public void deleteLastID() {

//         long delID = curId.decrementAndGet();
//         idTickets.remove(delID);
//     }

//     /**
//      * Adds a new ID to the set of unique IDs.
//      * 
//      * @param newId the new ID to add
//      */
//     public void addId(long newId) {

//         idTickets.add(newId);
//     }

//     /**
//      * Closes the file handler and releases any resources.
//      * 
//      * @throws IOException if an I/O error occurs
//      */
//     @Override
//     public void close() throws IOException {

//         fileHandler.close();
//     }
// }
