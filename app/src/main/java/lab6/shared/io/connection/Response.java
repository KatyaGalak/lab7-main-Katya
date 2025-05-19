package lab6.shared.io.connection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lab6.shared.ticket.Ticket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Response class represents a response to a command request in the application.
 * It encapsulates a message, an optional script, and a list of Ticket objects.
 */
@Getter
@Setter
@ToString
public class Response implements Serializable {
    private String message;
    private String script;
    private List<Ticket> tickets=new ArrayList<>();
    private Mark mark;
    private String command;
    private Integer list_index;

    /**
     * Constructs a new Response with the specified message, script, and an empty list of tickets.
     *
     * @param message the response message
     * @param script the script associated with the response
     */
    public Response( String message, String script) {
        this.message = message;
        this.script = script;
    }

    /**
     * Constructs a new Response with the specified message and list of tickets.
     *
     * @param message the response message
     * @param tickets the list of Ticket objects associated with the response
     */
    public Response( String message,  List<Ticket> tickets) {

        this.message = message;
        this.tickets = tickets;
    }

    /**
     * Constructs a new Response with the specified message and an array of Ticket objects.
     *
     * @param message the response message
     * @param tickets the Ticket objects associated with the response
     */
    public Response( String message,  Ticket... tickets) {

        this(message, List.of(tickets));
    }

    /**
     * Constructs a new Response with the specified message and an empty list of tickets.
     *
     * @param message the response message
     */
    public Response(String message) {
        this.message = message;
    }


    public Response(Mark mark, String message) {
        this.mark = mark;
        this.message = message;
    }

    public Response(Mark mark, String command, String message, List<Ticket> tickets) {
        this(message, tickets);
        this.mark = mark;
    }

    /**
     * Creates an empty Response instance.
     *
     * @return an empty Response
     */
    public static Response empty() {

        return new Response(null);
    }
}
