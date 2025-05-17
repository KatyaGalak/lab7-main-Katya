package lab6.server.system.commands;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.server.system.database.DatabaseManagerTicket;
import lab6.server.system.database.DatabaseManagerUser;
import lab6.server.system.usersRequest.PersonRequest;
import lab6.shared.io.connection.*;

/**
 * Command to remove a ticket from the collection by its ID.
 */
public class RemoveById extends Command {
    /**
     * Constructor for the RemoveById command.
     * Initializes the command with its name and description.
     */

    static final String[] args = new String[]{"id"};
    public RemoveById() {
        super("RemoveByID", "Delete an item by its ID", args);
    }

    /**
     * Executes the command to remove a ticket by its ID.
     *
     * @param request The request containing the arguments for the command.
     * @return A response indicating the result of the removal operation.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        if (request.getArgs() == null || request.getArgs().isEmpty()) {
            return new Response("ID of the item to be deleted is not set");
        }

        if (!isNumeric(request.getArgs().get(0))) {
            return new Response("Received ID is not a number");
        }

        final long delID = Long.parseLong(request.getArgs().get(0));
        if (!CollectionManager.getInstance()
                .getTicketCollection()
                .stream()
                .anyMatch(ticket -> ticket.getId() == delID))
            return new Response("There is no ticket with this ID");

        long userID = DatabaseManagerUser.getInstance().getUserId(request.getUserCredentials().username());

        if (!DatabaseManagerTicket.getInstance().checkIsOwnerForTicket(userID, delID)) {
                                return new Response("You can't delete an object that doesn't belong to you: " + delID);
        }

        if (!CollectionManager.getInstance().deleteByID(userID, delID))
            return new Response("Error when deleting an item from the database");

        CollectionManager.getInstance().getTicketCollection().removeIf(ticket -> {
            if (ticket.getId() == delID) {
                PersonRequest.deletePassportID(ticket.getPerson().getPassportID());
                return true;
            }
            return false;
        });

        return new Response("Ticket with ID: " + delID + " deleted");
    }    
}
