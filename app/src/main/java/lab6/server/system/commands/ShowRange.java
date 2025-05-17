package lab6.server.system.commands;

import java.util.ArrayList;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;

/**
 * Command to display the items in the collection.
 * This command retrieves the string representation of all items in the
 * collection.
 */
public class ShowRange extends Command {
    static final String[] args = new String[] { "start", "end" };

    /**
     * Constructor for the Show command.
     * Initializes the command with its name and description.
     */
    public ShowRange() {
        super("ShowRange", "Gets collection items in a string representation (start - end)", args);
    }

    /**
     * Executes the command to show the items in the collection.
     *
     * @param args The request containing the command arguments. Like [0] - start,
     *             [1] - end points
     * @return Response indicating the result of the command execution.
     */
    @Override
    public Response execute(Request args, SharedConsoleServer console) {
        Integer start = Integer.parseInt(args.getArgs().get(0));
        Integer end = Integer.parseInt(args.getArgs().get(1));

        if (CollectionManager.getInstance().getTicketCollection().isEmpty())
            return new Response("Collection is empty");
        int size = CollectionManager.getInstance().getTicketCollection().size();
        String msg = "";
        if (start >= size)
            start = size - 1;
        if (end >= size) {
            end = size;
            msg = "The end of collection";
        }
        return new Response("Collection items from " + start + " to " + end + ". " + msg,
                new ArrayList<>(CollectionManager.getInstance().getList().subList(start, end)));
    }
}
