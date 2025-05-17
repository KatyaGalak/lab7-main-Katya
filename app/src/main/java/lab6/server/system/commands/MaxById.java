package lab6.server.system.commands;

import java.util.Comparator;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.shared.io.connection.*;
import lab6.shared.ticket.Ticket;

/**
 * Command to retrieve and display the ticket with the maximum ID.
 */
public class MaxById extends Command {
    /**
     * Constructor for the MaxById command.
     * Initializes the command with its name and description.
     */


    public MaxById() {
        super("MaxById", "Get an item with the max ID");
    }

    /**
     * Executes the command to retrieve the ticket with the maximum ID.
     *
     * @param request The request containing the arguments for the command.
     * @return A response containing the ticket with the maximum ID or a message if the collection is empty.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {

        var maxElemOptional = CollectionManager.getInstance().getTicketCollection().stream().max(Comparator.comparing(Ticket::getId));

        if (!maxElemOptional.isPresent()) {
            return new Response("The collection is empty (no element with max ID)");
        }

        Ticket maxElem = maxElemOptional.get();

        return new Response("Element with max ID: ", maxElem);
    }
}
