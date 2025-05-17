package lab6.server.system.usersRequest;

import java.util.function.Predicate;

import lab6.server.SharedConsoleServer;
import lab6.shared.ticket.TicketType;

/**
 * The TicketTypeRequest class is responsible for handling user requests to input
 * ticket type information. It extends the Request class and provides methods to
 * create a TicketType object based on user input.
 */
public class TicketTypeRequest extends ClientRequest<TicketType> {

    /**
     * Constructs a new TicketTypeRequest with the specified console for user interaction.
     *
     * @param console the console instance used for input and output
     */
    public TicketTypeRequest(SharedConsoleServer console) {

        super(console);
    }

    /**
     * Creates a TicketType object based on user input for the ticket type.
     *
     * @return a TicketType object containing the user-provided value, or null if the input is invalid
    //  */
    // @Override
    public TicketType create() {

        Predicate<TicketType> predicateTicketType = x -> (x != null);
        TicketType ticketType = askEnum("Ticket type", TicketType.class, predicateTicketType);

        if (!predicateTicketType.test(ticketType))
            return null;

        return ticketType;
    }
}
