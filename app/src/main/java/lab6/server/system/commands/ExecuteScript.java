package lab6.server.system.commands;

import lab6.server.SharedConsoleServer;
import lab6.shared.io.connection.*;

/**
 * Command to execute a script file containing a series of commands.
 * This command reads the specified script file and executes the commands within it.
 */
public class ExecuteScript extends Command {
    static final String[] args = new String[]{"path"};

    /**
     * Constructor for the ExecuteScript command.
     * Initializes the command with its name and description.
     */
    public ExecuteScript() {
        super("ExecuteScript", "Execute script", args);
    }

    /**
     * Executes the command to run the specified script.
     *
     * @param request The request containing the command arguments.
     * @return Response indicating the result of the command execution.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {
        if (request.getArgs() == null || request.getArgs().isEmpty()) 
            return new Response("The path to the script was not passed.");
        
        return new Response("ScriptExecute " + request.getArgs().get(0));
    }
}
