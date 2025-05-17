package lab6.client.io;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Stack;

import lab6.client.io.exceptions.AlreadyAddedScript;
import lab6.client.network.SharedConsoleClient;
import lab6.shared.io.connection.UserCredentials;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The ScriptHandler class is responsible for executing scripts in the application. It extends
 * the Handler class and implements the AutoCloseable interface to manage resources properly.
 * The class maintains a stack of scanners to handle input from scripts and keeps track of
 * currently opening scripts to prevent recursion.
 */
public class ScriptHandler extends Handler implements AutoCloseable {

    private static HashSet<String> openingScripts = new HashSet<>();
    private static Stack<Scanner> scannerStack = new Stack<>();
    Path scriptPath;
    UserCredentials userCredentials;

    /**
     * Constructs a new ScriptHandler instance with the specified console and script path.
     * It initializes the scanner for the script and adds the script to the opening scripts set.
     *
     * @param console the console used for input and output operations
     * @param scriptPath the path to the script file to be executed
     * @throws IOException if an I/O error occurs while reading the script file
     */
    public ScriptHandler(SharedConsoleClient console, Path scriptPath, UserCredentials userCredentials) throws IOException {

        super(console);

        this.scriptPath = scriptPath;
        this.userCredentials = userCredentials;

        if (console.isFileScanner()) {
            scannerStack.add(console.getReader());
        }

        Scanner curScanner = new Scanner(Files.newBufferedReader(scriptPath));
        console.setScriptScanner(curScanner);

        openingScripts.add(scriptPath.getFileName().toString() + userCredentials.username());

    }

    /**
     * Returns the set of currently opening scripts.
     *
     * @return a HashSet containing the names of currently opening scripts
     */
    public static HashSet<String> getOpeningScripts() {

        return openingScripts;
    }

    /**
     * Closes the ScriptHandler, restoring the previous scanner and removing the script from
     * the opening scripts set.
     *
     * @throws IOException if an I/O error occurs while closing the scanner
     */
    @Override
    public void close() throws IOException {

        if (!scannerStack.isEmpty()) {
            console.setScriptScanner(scannerStack.pop());

        } else {
            console.setSimpleScanner();
        }

        if (!openingScripts.isEmpty()) {
            openingScripts.remove(scriptPath.getFileName().toString() + userCredentials.username());
        }
    }

    /**
     * Executes the script by reading lines from the script file and handling each line as a command.
     * If an error occurs during execution, it prints an error message to the console.
     */
    @Override
    public void run() {
        try {
            console.writeln("   # Executing script: " + scriptPath.getFileName());

            String line;
            while((line = console.read()) != null) {
                try {
                    handle(parse(line, userCredentials));
                } catch (AlreadyAddedScript e) {
                    System.err.println(e.getMessage());
                    return;
                }
                
            }
        } catch (Exception e) {
            console.writeln("   # Something went wrong: " + e.getMessage());
        }
    }
}