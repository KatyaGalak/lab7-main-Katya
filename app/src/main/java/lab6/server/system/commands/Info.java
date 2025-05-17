package lab6.server.system.commands;

import java.util.TreeSet;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.shared.io.connection.*;
import lab6.shared.ticket.Ticket;

/**
 * Command to retrieve and display information about the collection.
 */
public class Info extends Command {
    /**
     * Constructor for the Info command.
     * Initializes the command with its name and description.
     */


    public Info() {
        super("Info", "Gets information about the collection");
    }

    /**
     * Executes the command to retrieve information about the collection.
     *
     * @param request The request containing the arguments for the command.
     * @return A response containing the type, initialization date, and size of the collection.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {

        TreeSet<Ticket> collection = CollectionManager.getInstance().getTicketCollection();

        return new Response(String.join(System.lineSeparator(), new String[]{"type: " + collection.getClass(),
                                                                    "initialization date: " + CollectionManager.getInstance().getCreationDate(),
                                                                    "size: " + collection.size()}));
    }
}
