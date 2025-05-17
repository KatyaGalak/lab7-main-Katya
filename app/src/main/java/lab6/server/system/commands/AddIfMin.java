package lab6.server.system.commands;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.server.system.collection.TicketComparator;
import lab6.server.system.database.DatabaseManagerUser;
import lab6.server.system.usersRequest.TicketRequest;
import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;
import lab6.shared.ticket.Ticket;

/**
 * Command to add a new ticket to the collection if it is the minimum ticket.
 * This command compares the new ticket with the current minimum ticket in the
 * collection
 * and adds it only if it is smaller.
 */
public class AddIfMin extends Command {
    static final String[] args = new String[] { "name", "x", "y", "price", "refundable", "type", "person" };

    /**
     * Constructor for the AddIfMin command.
     * Initializes the command with its name, description, and required arguments.
     */
    public AddIfMin() {
        super("AddIfMin", "Add a new element if it becomes the min after adding it", args);
        // this.console = console;
    }

    /**
     * Executes the command to add a ticket to the collection if it is the minimum.
     *
     * @param request The request containing the command arguments.
     * @return Response indicating the result of the command execution.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        TicketRequest ticketRequest = new TicketRequest(console);

        Ticket ticket = ticketRequest.create();

        if (ticket == null) {
            return new Response(
                    "[Error] Ticket object was created with an error. The item was not compared and added to the collection");
        }

        var minElemOptional = CollectionManager.getInstance().getTicketCollection().stream()
                .min(new TicketComparator());

        if (!minElemOptional.isPresent())
            return new Response("The element (Ticket) to add is not specified");

        if (ticket.compareTo(minElemOptional.get()) < 0) {
            ticket.setCreatorId(DatabaseManagerUser.getInstance().getUserId(request.getUserCredentials().username()));

            if (CollectionManager.getInstance().add(ticket)) 
                return new Response("Element was added successfully, now it is the min in the collection.");

            return new Response("Ticket addition error (command AddIfMin).");
        }

        return new Response("Failure to add an element (it is not smaller than all the items in the collection).");
    }
}
