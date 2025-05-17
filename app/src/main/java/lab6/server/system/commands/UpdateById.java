package lab6.server.system.commands;

//import java.io.IOException;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.server.system.database.DatabaseManagerTicket;
import lab6.server.system.database.DatabaseManagerUser;
import lab6.server.system.usersRequest.TicketRequest;
import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;
import lab6.shared.ticket.Ticket;

/**
 * Command to update an existing ticket in the collection by its ID.
 */
public class UpdateById extends Command {
    static final String[] args = new String[] { "id" };

    public UpdateById() {
        super("UpdateByID", "Update the item with the passed ID.", args);
    }

    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        if (request.getArgs() == null || request.getArgs().isEmpty()) {
            return new Response("ID of the item to be updated is not set");
        }

        if (!isNumeric(request.getArgs().get(0))) {
            return new Response("Received ID is not a number");
        }

        long ID = Long.parseLong(request.getArgs().get(0));

        Ticket ticketToRemove = CollectionManager.getInstance().getTicketCollection().stream()
                .filter(ticketCmp -> ticketCmp.getId() == ID)
                .findFirst()
                .orElse(null);

        if (ticketToRemove == null) {
            return new Response("There is no ticket with this ID");
        }

        long userID = DatabaseManagerUser.getInstance().getUserId(request.getUserCredentials().username());

        if (!DatabaseManagerTicket.getInstance().checkIsOwnerForTicket(userID, ID)) 
                                return new Response("You can't update an object that doesn't belong to you: " + ID);

        TicketRequest ticketRequest = new TicketRequest(console);

        Ticket ticket = ticketRequest.create();

        if (ticket == null) {
            return new Response("[Error] Ticket object was created with an error. The item was not updated");
        }

        

        ticket.setId(ticketToRemove.getId());
        ticket.setCreatorId(ticketToRemove.getCreatorId());

        //CollectionManager.getInstance().getTicketCollection().remove(ticketToRemove);

        //CollectionManager.getInstance().getTicketCollection().add(ticket);
        if (!CollectionManager.getInstance().updateTicketByID(ticketToRemove, ticket)) {
            return new Response("Error updating an element");
        }
        


        //new Save().execute(null, null);

        return new Response("The element with ID: " + ID + " has been updated");
    }
}
