package lab6.shared.io.console;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * The StandartConsole class implements the Console interface, providing methods
 * for
 * handling input and output operations in a standard console environment. It
 * allows
 * reading user input, writing output, and managing the scanner used for input.
 */
public class StandartConsole implements Console {
    private static Scanner reader = new Scanner(System.in);
    private static final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
    private static boolean isScript = false;

    public Scanner getReader() {
        return reader;
    }

    public StandartConsole() {
        super();
    }

    /**
     * Writes a string of data to the console.
     *
     * @param data the data to be written to the console
     */
    public void write(String data) {
        try {
            writer.append(data);
            writer.flush();
        } catch (IOException e) {
        }
    }

    /**
     * Writes a line of data to the console, appending a newline character.
     *
     * @param data the data to be written to the console
     */
    public void writeln(String data) {
        try {
            writer.append(data).append(System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
        }
    }

    /**
     * Reads a line of input from the user.
     *
     * @return the input string provided by the user, or null if no input is
     *         available
     */
    public String read() {

        try {
            return reader.nextLine();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Reads a line of input from the user, displaying the specified prompt.
     *
     * @param prompt the message to display to the user
     * @return the input string provided by the user
     */
    public String read(String prompt) {

        writeln(prompt);

        return read();
    }

    /**
     * Sets the scanner to read from a script.
     *
     * @param scanner the Scanner instance to be set for script reading
     */
    public void setScriptScanner(Scanner scanner) {

        reader = scanner;
        isScript = true;
    }

    /**
     * Checks if the console is using a file scanner.
     *
     * @return true if the console is using a file scanner, false otherwise
     */
    public boolean isFileScanner() {

        return isScript;
    }

    /**
     * Sets the scanner to read from standard input.
     */
    public void setSimpleScanner() {

        isScript = false;
        reader = new Scanner(System.in);
    }
}
