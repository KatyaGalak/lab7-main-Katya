package lab6.server.system.commands;

import java.util.Arrays;
import java.util.logging.Logger;


import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.server.system.usersRequest.ClientRequest;
import lab6.shared.io.connection.Mark;
import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;
import lab6.server.Server;

import lombok.Getter;
import lombok.Setter;

/**
 * Command to display the items in the collection.
 * This command retrieves the string representation of all items in the
 * collection.
 */
public class Show extends Command {
    @Setter
    @Getter
    private int count = 10;

    private Server server;

    private static final Logger logger = Logger.getLogger(Show.class.getName());

    /**
     * Constructor for the Show command.
     * Initializes the command with its name and description.
     */
    public Show(Server server) {
        super("Show", "Gets collection items in a string representation");

        this.server = server;
    }

    /**
     * Executes the command to show the items in the collection.
     *
     * @param args The request containing the command arguments.
     * @return Response indicating the result of the command execution.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {

        if (CollectionManager.getInstance().getTicketCollection().isEmpty()) {
            server.completeInteractive(console.getClientAddress());
            return new Response(Mark.COMPLETED_SHOW,"Collection is empty");
        }

        Integer start = 0;
        if (request.getMark() == Mark.WAIT_NEXT && request.getArgs() != null && !request.getArgs().isEmpty()) {
            try {
                start = Integer.parseInt(request.getArgs().get(0));
                logger.info("[SHOW] Continuing with start=" + start + " for client: " + console.getClientAddress());
            } catch (NumberFormatException e) {
                logger.warning("Invalid start index: " + request.getArgs().get(0) + " for client: " + console.getClientAddress());
                start = 0;
            }
        }

        int collectionSize = CollectionManager.getInstance().getTicketCollection().size();
        int end = Math.min(start + count, collectionSize);
        logger.info("[SHOW] Processing segment start=" + start + ", end=" + end + " for client: " + console.getClientAddress());
        Response response = new ShowRange().execute(
                new Request("show", Arrays.asList(String.valueOf(start), String.valueOf(end)), request.getUserCredentials()),
                console);
        response.setList_index(end);
        response.setCommand("show");
        response.setMessage("Collection items from " + start + " to " + end + ".");

        console.writeln(response, (end < collectionSize));

        if (end < collectionSize) {
            logger.info("[SHOW] Requesting continuation for client: " + console.getClientAddress());
            Boolean continueShow = new ClientRequest<Boolean>(console).askBoolean(
                    "Answer to continue",
                    "TRUE or FALSE",
                    x -> x != null && (x.equalsIgnoreCase("TRUE") || x.equalsIgnoreCase("FALSE")));
            logger.info("[SHOW] Received answer: " + continueShow + " from client: " + console.getClientAddress());

            if (continueShow == null || !continueShow) {
                Response finalResponse = new Response(Mark.COMPLETED_SHOW, "Show terminated by client");
                return finalResponse;
            }

            // Рекурсивно вызываем execute для следующего кусочка вывода
            return execute(new Request(Mark.WAIT_NEXT, "show", Arrays.asList(String.valueOf(end)), request.getUserCredentials()), console);
        }

        Response finalResponse = new Response(Mark.COMPLETED_SHOW, "Show completed");
        return finalResponse;
    }
}
