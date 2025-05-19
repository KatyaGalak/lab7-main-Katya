package lab6.server.system.commands;

import java.util.List;

import lab6.server.Server;

/**
 * A utility class that holds a list of all added command instances.
 * This class provides a method to retrieve the list of commands that can be executed.
 */
public final class AddedCommands {
    private static List<Command> addedCommands = List.of(
        new Add(),
        new AddIfMax(),
        new AddIfMin(),
        new Clear(),
        new CountLessThanType(),
        new Exit(),
        new FilterContainsName(),
        new Help(),
        new History(),
        new Info(),
        new MaxById(),
        new RemoveById(),
        new RemoveGreater(),
        // new Save(),
        new Show(Server.getInstance()),
        new UpdateById(),
        new ExecuteScript(),
        new AddRandom()
    );

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AddedCommands() {}

    /**
     * Retrieves the list of added commands.
     *
     * @return A list of Command instances that have been added.
     */
    public static List<Command> getAddedCommands() {
        return addedCommands;
    }
}
