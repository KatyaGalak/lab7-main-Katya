package lab6.server.system.commands;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lab6.server.SharedConsoleServer;
import lab6.server.system.commands.util.HistoryManager;
import lab6.shared.io.connection.*;

/**
 * Command to retrieve and display the last 15 executed commands.
 */
public class History extends Command {
    /**
     * Constructor for the History command.
     * Initializes the command with its name and description.
     */


    public History() {
        super("History", "Get the last 15 commands");
    }

    /**
     * Executes the command to retrieve the last 15 commands.
     *
     * @param request The request containing the arguments for the command.
     * @return A response containing the history of the last 15 commands.
     */
    @Override
    public Response execute(Request request, SharedConsoleServer console) {

        List<String> history = HistoryManager.getInstance().getHistory();

        String historyWithLineNumbers = IntStream.range(0, history.size())
                .mapToObj(i -> (i + 1) + ": " + history.get(i))
                .collect(Collectors.joining(System.lineSeparator()));

        return new Response(historyWithLineNumbers);
    }
}
