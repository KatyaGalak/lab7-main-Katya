package lab6.server.system.commands;

import lab6.server.SharedConsoleServer;
import lab6.shared.io.connection.*;

/**
 * Command to provide help on available commands.
 */
public class Help extends Command {
    /**
     * Constructor for the Help command.
     * Initializes the command with its name and description.
     */


    public Help() {
        super("Help", "Get help on available commands");
    }

    /**
     * Executes the command to provide help on available commands.
     *
     * @param request The request containing the arguments for the command.
     * @return A response containing the help information for available commands.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {

        StringBuilder res = new StringBuilder();

        for (Command command : AddedCommands.getAddedCommands()) {
            res.append(command.getName()).append(" - ").append(command.getDescription()).append(System.lineSeparator());
            if (command.getArgs() != Command.EMPTY_ARGUMENTS) {
                res.append("args: (").append(String.join(", ", command.getArgs())).append(")").append(System.lineSeparator());
            }
            res.append(System.lineSeparator());
        }


        return new Response(res.toString());
    }
}
