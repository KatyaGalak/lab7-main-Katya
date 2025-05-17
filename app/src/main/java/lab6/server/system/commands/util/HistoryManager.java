package lab6.server.system.commands.util;

import java.util.LinkedList;
import java.util.List;

/**
 * Manages the history of commands executed in the application.
 * This class maintains a limited number of the most recent commands.
 */
public class HistoryManager {
    private static final int NUM_ELEMENTS_HISTORY = 15;

    private LinkedList<String> lastCommands = new LinkedList<>();
    private static HistoryManager historyManager;

    /**
     * Private constructor to prevent instantiation.
     */
    private HistoryManager() {}

    /**
     * Returns the singleton instance of the HistoryManager.
     *
     * @return The singleton instance of HistoryManager.
     */
    public static HistoryManager getInstance() {
        return historyManager == null ? historyManager = new HistoryManager() : historyManager;
    }

    /**
     * Adds a command to the history.
     * If the history exceeds the maximum number of elements, the oldest command is removed.
     *
     * @param command The command to be added to the history.
     */
    public void addCommand(String command) {
        lastCommands.add(command);
        if (lastCommands.size() > NUM_ELEMENTS_HISTORY) {
            lastCommands.removeFirst();
        }
    }

    /**
     * Retrieves the list of commands in the history.
     *
     * @return A list of the last commands executed.
     */
    public List<String> getHistory() {
        return lastCommands;
    }
}
