package lab6.server.system.commands;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.server.system.database.DatabaseManagerUser;
import lab6.server.system.usersRequest.PersonRequest;
import lab6.server.system.usersRequest.TicketRequest;
import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;
import lab6.shared.ticket.Ticket;

/**
 * Command to remove tickets from the collection that are greater than the
 * specified ticket.
 */
public class RemoveGreater extends Command {
    static final String[] args = new String[] { "name", "x", "y", "price", "refundable", "type", "person" };

    /**
     * Constructor for the RemoveGreater command.
     * Initializes the command with its name and description.
     */
    public RemoveGreater() {
        super("RemoveGreater", "Delete items from the collection that are greater than the specified item", args);
        // this.console = console;
    }


    /**
     * Executes the command to remove tickets from the collection that are greater
     * than the specified ticket.
     *
     * @param request The request containing the ticket information.
     * @return A response indicating the result of the removal operation.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        TicketRequest ticketRequest = new TicketRequest(console);

        Ticket ticket = ticketRequest.create();

        if (ticket == null) {
            return new Response(
                    "[Error] Ticket object was created with an error. The item was not compared and added to the collection");
        }


        long userID = DatabaseManagerUser.getInstance().getUserId(request.getUserCredentials().username());
        
        CollectionManager.getInstance().getTicketCollection().removeIf(cmpTicket -> {
            if (cmpTicket.compareTo(ticket) > 0 && cmpTicket.getCreatorId() == userID) {
                PersonRequest.deletePassportID(cmpTicket.getPerson().getPassportID());
                CollectionManager.getInstance().deleteByID(userID, cmpTicket.getId());
                return true;
            }
            return false;
        });

        //new Save().execute(null, null);

        PersonRequest.deletePassportID(ticket.getPerson().getPassportID());

        return new Response("Collection items greater than the specified value have been deleted (if there were any).");
    }    
}
