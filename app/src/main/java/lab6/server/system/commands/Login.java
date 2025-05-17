package lab6.server.system.commands;

import lab6.server.SharedConsoleServer;
import lab6.shared.io.connection.*;
import lab6.server.system.database.DatabaseManagerUser;

public class Login extends Command {


    public Login() {
        super("Login", "User login (registration has already been completed)");
    }

    @Override
    public Response execute(Request request, SharedConsoleServer console) {

        if (DatabaseManagerUser.getInstance().getUserId(request.getUserCredentials().username()) == -1)
            return new Response("Login Error:" + " there is no user with name " 
                                                + request.getUserCredentials().username());

        boolean checkPassword = DatabaseManagerUser.getInstance().checkPassword(request.getUserCredentials());

        if (checkPassword) {
            return new Response("Login completed successfully");
        }

        return new Response("Login Error: " + "password doesn't match");
    }
}