package lab6.server.system.commands;

import lab6.server.SharedConsoleServer;
import lab6.server.system.collection.CollectionManager;
import lab6.server.system.database.DatabaseManagerUser;
import lab6.server.system.usersRequest.TicketRequest;
import lab6.shared.io.connection.*;
import lab6.shared.ticket.Ticket;

public class Add extends Command {
    static final String[] args = new String[] { "name", "x", "y", "price", "refundable", "type", "person" };
    // private SharedConsoleServer console;

    public Add() {
        super("Add", "Add a new element to the collection", args);
    }

    // public void setConsole(SharedConsoleServer console) {
    //     this.console = console;
    // }

    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        TicketRequest ticketRequest = new TicketRequest(console);

        Ticket ticket = ticketRequest.create();

        if (ticket == null) {
            return new Response(
                    "[Error] Ticket object was created with an error. The item was not added to the collection");
        }

        ticket.setCreatorId(DatabaseManagerUser.getInstance().getUserId(request.getUserCredentials().username()));

        if (CollectionManager.getInstance().add(ticket)) 
            return new Response("Ticket added");

        return new Response("Ticket addition error");
    }
}