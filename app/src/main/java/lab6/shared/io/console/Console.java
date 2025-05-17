package lab6.shared.io.console;

import java.io.Serializable;
import java.util.Scanner;

/**
 * The Console interface defines the methods for handling input and output operations
 * in the application. It extends the IOHandler interface to provide functionality
 * for reading user input and writing output to the console.
 */
public interface Console extends Serializable {

    /**
     * Reads a line of input from the user, displaying the specified prompt.
     *
     * @param prompt the message to display to the user
     * @return the input string provided by the user
     */
    public String read();
    public String read(String prompt);

    /**
     * Writes a line of data to the console.
     *
     * @param data the data to be written to the console
     */
    public void writeln(String data);

    public void write(String dara);

    /**
     * Checks if the console is using a file scanner.
     *
     * @return true if the console is using a file scanner, false otherwise
     */
    public boolean isFileScanner();

    /**
     * Sets the scanner to read from a script.
     *
     * @param scanner the Scanner instance to be set for script reading
     */
    public void setScriptScanner(Scanner scanner);

    /**
     * Sets the scanner to read from standard input.
     */
    public void setSimpleScanner();


    /**
     * Gets the current Scanner instance used for reading input.
     *
     * @return the Scanner instance
     */
    public Scanner getReader();

}
