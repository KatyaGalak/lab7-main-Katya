package lab6.server.system.commands;

import java.util.List;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.server.system.commands.util.CreateRandomTicket;
import lab6.server.system.database.DatabaseManagerUser;
import lab6.shared.io.connection.*;
import lab6.shared.ticket.Ticket;

/**
 * Command to add a specified number of random tickets to the collection.
 * This command generates random tickets and adds them to the existing collection.
 */
public class AddRandom extends Command {
    static final String[] args = new String[]{"cnt"};

    /**
     * Constructor for the AddRandom command.
     * Initializes the command with its name and description.
     */
    public AddRandom() {
        super("AddRandom", "Add a set number of random tickets", args);
    }

    /**
     * Executes the command, adding random tickets to the collection.
     *
     * @param request The request containing the command arguments.
     * @return Response with the result of the command execution.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        int cntTickets = 0;

        if (request.getArgs() == null || request.getArgs().isEmpty()) {
            cntTickets = 1;
        } else if (!isNumeric(request.getArgs().get(0))) {
            return new Response("The transferred arg is not a number");
        } else {
            cntTickets = Integer.parseInt(request.getArgs().get(0));
        }
        try {
            List<Ticket> randomTickets = CreateRandomTicket.generateRandomTicket(cntTickets);
            for (Ticket ticket : randomTickets) {
                ticket.setCreatorId(DatabaseManagerUser.getInstance().getUserId(request.getUserCredentials().username()));
                CollectionManager.getInstance().add(ticket);
            }
           // CollectionManager.getInstance().getTicketCollection().addAll(randomTickets);
            //new Save().execute(null, null);
        } catch (IllegalArgumentException e) {
            return new Response("The problem with the generated data");
        }
        
        return new Response("Random Tickets added (" + cntTickets + ").");
    }
}
