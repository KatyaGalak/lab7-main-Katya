package lab6.server.system.commands;

import lab6.server.SharedConsoleServer;
import lab6.shared.io.connection.*;
import lombok.Getter;

/**
 * Abstract class representing a command in the application.
 * This class serves as a base for all specific command implementations.
 * Each command has a name, description, and a set of required arguments.
 */
public abstract class Command {
    public static final String[] EMPTY_ARGUMENTS = new String[0];
    private static final int EMPTY_CNT_ARGUMENTS = 0;

    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final String[] args;
    @Getter
    private final int cntArgs;

    /**
     * Constructor for the Command class.
     * Initializes the command with its name, description, and required arguments.
     *
     * @param name        The name of the command.
     * @param description A brief description of what the command does.
     * @param args        An array of required arguments for the command.
     * @param cntArgs     Number of required arguments.
     */
    public Command(final String name, final String description, 
                    final String[] args, final int cntArgs) {
        this.name = name;
        this.description = description;
        this.args = args;
        this.cntArgs = cntArgs;
    }

    /**
     * Constructor for the Command class.
     * Initializes the command with its name, description, and required arguments.
     *
     * @param name        The name of the command.
     * @param description A brief description of what the command does.
     * @param args        An array of required arguments for the command.
     */
    public Command(final String name, final String description, 
                    final String[] args) {
        this(name, description, args, args.length);
    }

    /**
     * Constructor for the Command class.
     * Initializes the command with its name, description, and required arguments.
     *
     * @param name        The name of the command.
     * @param description A brief description of what the command does.
     */
    public Command(final String name, final String description) {
        this(name, description, EMPTY_ARGUMENTS, EMPTY_CNT_ARGUMENTS);
    }

    /**
     * Executes the command with the provided request.
     *
     * @param request The request containing the command arguments.
     * @return Response indicating the result of the command execution.
     */
    public abstract Response execute(Request request, SharedConsoleServer console);


    /**
     * Checks if the given string is a numeric value.
     *
     * @param str The string to check.
     * @return true if the string is numeric, false otherwise.
     */
    protected boolean isNumeric(String str) {

        if (str == null || str.isEmpty()) 
            return false;

        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
