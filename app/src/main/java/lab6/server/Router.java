package lab6.server;

import lab6.server.system.commands.AddedCommands;
import lab6.server.system.commands.util.HistoryManager;
import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;
import lab6.server.system.commands.Login;
import lab6.server.system.commands.Registration;
import lab6.server.system.commands.Show;

public class Router {
    private SharedConsoleServer console;
    private Server server;

    public Router(SharedConsoleServer console, Server server) {
        this.console = console;
        this.server = server;
    }

    public Response route(Request request) {
        if (request == null || request.getCommand() == null || request.getCommand().isBlank()) {
            return Response.empty();
        }

        if (request.getCommand().equalsIgnoreCase("Login"))
            return new Login().execute(request, console);

            if (request.getCommand().equalsIgnoreCase("Registration"))
            return new Registration().execute(request, console);

        return AddedCommands.getAddedCommands().stream()
            .filter(name -> name.getName().equalsIgnoreCase(request.getCommand()))
            .findFirst()
            .map(temp -> {
                HistoryManager.getInstance().addCommand(temp.getName());
    
                if (temp.getName().equalsIgnoreCase("show")) {
                    return new Show(server).execute(request, console);
                }
    
                return temp.execute(request, console);
            }).orElse(new Response("Command not found"));

        // return AddedCommands.getAddedCommands().stream()
        //         .filter(name -> name.getName().equalsIgnoreCase(request.getCommand()))
        //         .findFirst()
        //         .map(temp -> {
        //             HistoryManager.getInstance().addCommand(temp.getName());

        //             if (name.getName().equalsIgnoreCase("show")) {
        //                 return new Show(server).execute(request, console);
        //             }

        //             // if (temp.getName().equalsIgnoreCase("show")) {
        //             //     return new Show(server).execute(request, console);
        //             // }

        //             return temp.execute(request, console);
        //         }).orElse(new Response("Command not found"));
    }
}