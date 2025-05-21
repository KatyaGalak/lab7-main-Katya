package lab6.client.io;

import java.util.List;
import java.util.function.Predicate;
import java.util.Arrays;
import java.util.Collections;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lab6.client.io.exceptions.AlreadyAddedScript;
import lab6.client.network.SharedConsoleClient;
import lab6.shared.io.connection.Mark;
import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;
import lab6.shared.ticket.Ticket;
import lab6.shared.io.connection.UserCredentials;

public class Handler implements Runnable {
    public final SharedConsoleClient console;

    /**
     * Constructs a new Handler instance with the specified console.
     *
     * @param console the console used for input and output operations, expected to
     *                be SharedConsoleClient
     */
    public Handler(SharedConsoleClient console) {
        this.console = console;
    }

    public Request parse(String data, UserCredentials userCredentials) {
        if (data == null)
            return null;

        String[] parts = data.strip().split("\\s+", 2);
        String nameCommand = parts[0].strip();
        final List<String> args = parts.length > 1 ? Arrays.asList(parts[1].split("\s+")) : Collections.emptyList();
        return new Request(nameCommand, args, userCredentials);
    }

    public void printConsole(Response response) {
        if (response == null)
            return;
        if (response.getMessage() != null && !response.getMessage().isBlank() &&
                (response.getTickets() == null || response.getTickets().size() == 0)) {
            console.writeln(response.getMessage());
        } else if (response.getTickets() != null && response.getTickets().size() != 0) {
            if (response.getMessage() != null && !response.getMessage().isBlank()) {
                console.writeln(response.getMessage());
            }
            response.getTickets().stream().map(Ticket::toString).forEach(console::writeln);
        }
    }

    // @Override
    public void handle(Request request) throws AlreadyAddedScript {

        // Отправляем команду на сервер через SharedConsoleClient
        Response response = ((SharedConsoleClient) console).getClient().sendRecive(request);

        if (response == null) {
            return;
        }

        // Обработка скриптов
        if (response.getMessage() != null && response.getMessage().contains("ScriptExecute")) {
            String scriptPathString = response.getMessage().substring("ScriptExecute".length() + 1).trim();
            Path scriptPath = Path.of(scriptPathString);

            if (!Files.exists(scriptPath)) {
                console.writeln("   # IO Error: File " + scriptPath.getFileName() + " does not exist");
                return;
            }
            if (!Files.isRegularFile(scriptPath)) {
                console.writeln("   # File " + scriptPath.getFileName() + " is not a regular file");
                return;
            }
            if (!Files.isReadable(scriptPath)) {
                console.writeln("   # File " + scriptPath.getFileName() + " cannot be read");
                return;
            }

            if (ScriptHandler.getOpeningScripts().contains(scriptPath.getFileName().toString() + request.getUserCredentials().username())) {
                console.writeln("   # Recursion detected, repeated call: " + scriptPath.getFileName());
                throw new AlreadyAddedScript("  # Completing the execution of the current script (Recursion detected)");
            }

            try (ScriptHandler scriptHandler = new ScriptHandler(console, scriptPath, request.getUserCredentials())) {
                scriptHandler.run();
            } catch (IOException e) {
                console.writeln("   # IO Error: " + e.getMessage());
            }
        }

        printConsole(response);

        // Обработка марки запроса ввода
        if (response.getMark() != null) {
            switch (response.getMark()) {
                case COMPLETED_SHOW:
                    return;
                case WAIT_NEXT:
                    //console.writeln("Received WAIT_NEXT, continuing with start index: " + response.getList_index());
                    handle(new Request(Mark.WAIT_NEXT, request.getCommand(), Arrays.asList(response.getList_index().toString()), request.getUserCredentials()));
                    return;
                case INPUT_REQUEST:
                    String input = console.read();
                    //console.writeln("Sending INPUT_RESPONCE: " + input + " for command: " + request.getCommand());
                    handle(new Request(Mark.INPUT_RESPONCE, request.getCommand(), Arrays.asList(input), request.getUserCredentials()));
                    return;
                
                default:
                    //console.writeln("DEBUG: Unknown mark: " + response.getMark());
                    return;
            }
        }

        if (request.getCommand().equals("show") && response.getTickets() != null) {
            //console.writeln("DEBUG: Continuing show command, expecting COMPLETED_SHOW");
            Response finalResponse = ((SharedConsoleClient) console).getClient().sendRecive(null);

            if (finalResponse != null)
                printConsole(finalResponse);
            return;
        }

        if (response.getMessage() != null && response.getMessage().contains("Command Exit")) {
            System.exit(0);
        }

        //console.writeln("DEBUG: Exiting handle for request: " + request);
    }

    private String askStringUser(String name, String limitations, Predicate<String> predicate) {
        StringBuilder promt = new StringBuilder("");
        while (true) {
            promt.append("  # Enter " + name + (limitations == null ? "" : ("; field restrictions: " + limitations)) + " :");

            String input = console.read(promt.toString());
            if (input != null)
                input = input.trim();

            if (input == "" && predicate.test(""))
                return null;
            
            if (predicate.test(input))
                return input;
            else {
                promt = new StringBuilder("  # Incorrect data for " + name + "; make sure that the entered data meets the restrictions: " + limitations + "\n and try again.");
            }
        }
    }

    private UserCredentials getUserCredentialsInput() {
        String userName = askStringUser("UserName", "The UserName cannot be empty", x -> (x != null));
        String password = askStringUser("Password", "The password cannot be empty", x -> (x != null));
        return new UserCredentials(userName, password);
    }

    private UserCredentials registration() {
        UserCredentials userCredentialsReg = getUserCredentialsInput();

        Request regRequest = new Request("Registration", userCredentialsReg);
        Response response = ((SharedConsoleClient) console).getClient().sendRecive(regRequest);

        printConsole(response);

        if (response == null || response.getMessage().contains("Registration Error")) 
            return UserCredentials.getEmptyUserCredentials();
        
        return userCredentialsReg;
    }

    private UserCredentials login() {
        UserCredentials userCredentialsReg = getUserCredentialsInput();

        Request regRequest = new Request("Login", userCredentialsReg);
        Response response = ((SharedConsoleClient) console).getClient().sendRecive(regRequest);

        printConsole(response);

        if (response == null || response.getMessage().contains("Login Error"))
            return UserCredentials.getEmptyUserCredentials();

        return userCredentialsReg;
    }

    private UserCredentials startUser() {
        return
            switch (console.read("  # Hello! if you want to register, enter 'registration'. If you already have an account, enter 'login'.")
                            .toLowerCase()) {
                case "registration" -> registration();
                case "login" -> login();
                default -> UserCredentials.getEmptyUserCredentials();
            };
    }

    @Override
    public void run() {
        UserCredentials userCredentials;

        while((userCredentials = startUser()).equals(UserCredentials.getEmptyUserCredentials()));

        console.writeln("   # Welcome! Let's start working with the collection. :)");

        String line;
        while ((line = console.read(": ")) != null) {
            process(line, userCredentials);
        }
    }


    public void process(String line, UserCredentials userCredentials) {
        try {
            handle(parse(line, userCredentials));
        } catch (AlreadyAddedScript e) {
            console.writeln(e.getMessage());
        }
    }
}