package lab6.server.system.commands;

import lab6.server.SharedConsoleServer;
import lab6.shared.io.connection.*;
import lab6.server.system.database.DatabaseManagerUser;

public class Registration extends Command {


    public Registration() {
        super("Registration", "User registration (you need to specify a username and password)");
    }

    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        if (DatabaseManagerUser.getInstance().getUserId(request.getUserCredentials().username()) != -1)
            return new Response("Registration Error:" + " user with that name already exists");

        boolean addUser = DatabaseManagerUser.getInstance().addUser(request.getUserCredentials());

        return addUser ? new Response("User with name " + request.getUserCredentials().username() + " successfully registered")
                        : new Response("Registration Error:" + "sth went wrong during registration");
    }
}