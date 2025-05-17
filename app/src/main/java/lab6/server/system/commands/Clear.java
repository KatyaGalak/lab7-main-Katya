package lab6.server.system.commands;

//import java.util.Collection;
import java.util.List;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.server.system.database.DatabaseManagerTicket;
import lab6.server.system.database.DatabaseManagerUser;
import lab6.server.system.usersRequest.PersonRequest;
import lab6.shared.io.connection.*;
//import lab6.shared.ticket.Ticket;

/**
 * Command to clear the collection of tickets.
 * This command removes all tickets from the collection, effectively resetting it.
 */
public class Clear extends Command {

    /**
     * Constructor for the Clear command.
     * Initializes the command with its name and description.
     */
    public Clear() {
        super("Clear", "Clear the collection");
    }

    /**
     * Executes the command to clear the ticket collection.
     *
     * @param request The request containing the command arguments.
     * @return Response indicating the result of the command execution.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        //Collection<Ticket> tickets = CollectionManager.getInstance().getTicketCollection();

        long userID = DatabaseManagerUser.getInstance().getUserId(request.getUserCredentials().username());

        List<String> passportIDClear = DatabaseManagerTicket.getInstance().getPassportIDTicketsByUser(userID);
        
        if (!CollectionManager.getInstance().deleteByUser(userID)) {
            return new Response("Error when deleting items.");
        }

        passportIDClear.stream()
               .forEach(passportID -> PersonRequest.deletePassportID(passportID)); // удалили passportID удаленных

        CollectionManager.getInstance().getTicketCollection().removeIf(ticket -> ticket.getCreatorId() == userID);
        
        //CollectionManager.getInstance().getTicketCollection().clear();
        //new Save().execute(null, null);
    
        return new Response("Collection has been successfully cleared (The objects belonging to you have been deleted).");
    }    
}
