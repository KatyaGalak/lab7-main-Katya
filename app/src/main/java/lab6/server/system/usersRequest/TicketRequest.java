package lab6.server.system.usersRequest;

import java.util.function.Predicate;

import lab6.server.SharedConsoleServer;
import lab6.shared.ticket.Coordinates;
import lab6.shared.ticket.Person;
import lab6.shared.ticket.Ticket;
import lab6.shared.ticket.TicketType;


/**
 * The TicketRequest class is responsible for handling user requests to input
 * ticket information, including the ticket's name, coordinates, price, 
 * refundability, ticket type, and associated person. It extends the Request class
 * and provides methods to create a Ticket object based on user input.
 */
public class TicketRequest extends ClientRequest<Ticket> {

    private final SharedConsoleServer console;

    /**
     * Constructs a new TicketRequest with the specified console for user interaction.
     *
     * @param console the console instance used for input and output
     */
    public TicketRequest(SharedConsoleServer console) {

        super(console);
        this.console = console;
    }


    /**
     * Prompts the user to input the ticket's coordinates.
     *
     * @return a Coordinates object representing the ticket's coordinates
     */
    private Coordinates askCoordinates() {

        return new CoordinatesRequest(console).create();
    }

    /**
     * Prompts the user to input the ticket type.
     *
     * @return a TicketType object representing the ticket type
     */
    private TicketType askTicketType() {

        return new TicketTypeRequest(console).create();
    }

    /**
     * Prompts the user to input the associated person's information.
     *
     * @return a Person object representing the associated person
     */
    private Person askPerson() {

        return new PersonRequest(console).create();
    }

    /**
     * Creates a Ticket object based on user input for the ticket's name, coordinates,
     * price, refundability, ticket type, and associated person.
     *
     * @return a Ticket object containing the user-provided values, or null if the input is invalid
     */
    // @Override
    public Ticket create() {

        Predicate<String> predicateName = x -> (x != null && x.length() > 0);
        String name = askString("Name Ticket", "The value cannot be missing or have a length of 0", predicateName);

        Predicate<Coordinates> predicateCoordinates = x -> (x != null);
        Coordinates coordinates = askCoordinates();

        Predicate<Double> predicatePrice = x -> (x == null || x > 0);
        Double price = askNumericValue("Ticket price", "The value must be greater then 0", predicatePrice, Double.class);

        Predicate<Boolean> predicateRefundable = x -> true;
        Boolean refubdable = askBoolean("Returnability of the Ticket", "TRUE or FALSE", x -> (x.isEmpty() || x == null || x.equalsIgnoreCase("TRUE") || x.equalsIgnoreCase("FALSE")));

        Predicate<TicketType> predicateTicketType = x -> (x != null);
        TicketType ticketType = askTicketType();

        Predicate<Person> predicatePerson = x -> (x != null);
        Person person = askPerson();

        if (!predicateName.test(name))
        return null;

        if (!predicateCoordinates.test(coordinates))
            return null;

        if (!predicatePrice.test(price))
            return null;

        if (!predicateRefundable.test(refubdable))
            return null;

        if (!predicateTicketType.test(ticketType))
            return null;

        if (!predicatePerson.test(person))
            return null;

        Ticket ticket = new Ticket(name, coordinates, price, refubdable, ticketType, person);
 
        return ticket;
    }
}
