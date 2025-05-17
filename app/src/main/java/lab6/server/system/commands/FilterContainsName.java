package lab6.server.system.commands;

import java.util.List;
import java.util.stream.Collectors;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;
import lab6.shared.ticket.Ticket;

/**
 * Command to filter tickets whose names contain a specified substring.
 */
public class FilterContainsName extends Command {
    /**
     * Constructor for the FilterContainsName command.
     * Initializes the command with its name, description, and arguments.
     */

    static final String[] args = new String[] { "name" };

    public FilterContainsName() {
        super("FilterContainsName", "Get the elements whose name contains the specified string", args);
    }

    /**
     * Executes the command to filter tickets based on the provided request.
     *
     * @param request The request containing the arguments for the command.
     * @return A response containing the filtered tickets or an error message.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        if (request.getArgs().get(0).isEmpty()) {
            return new Response("Passed string is empty");
        }

        List<Ticket> nameWithSubstr = CollectionManager.getInstance().getTicketCollection().stream()
                .filter(ticket -> ticket.getName().contains(request.getArgs().get(0)))
                .collect(Collectors.toList());

        if (nameWithSubstr.isEmpty()) {
            return new Response(
                    "No elements in the collection whose name contains substring: " + request.getArgs().get(0));
        }

        return new Response("Elements with the specified substr (" + request.getArgs().get(0) + ") in their name",
                nameWithSubstr);
    }
}
