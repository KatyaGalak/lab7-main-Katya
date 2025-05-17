package lab6.server.system.file;

import java.nio.file.Path;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.Scanner;

import java.io.IOException;
import java.io.OutputStreamWriter;

import lombok.Getter;

public class FileHandler {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    private final Path filePath;

    @Getter
    private final Scanner scanner;

    @Getter
    private final OutputStreamWriter outputStreamWriter;

    private final boolean mode;

    public void close() throws IOException {
        scanner.close();
        outputStreamWriter.close();
    }

    public FileHandler(Path filePath, boolean mode) throws IOException {
        this.filePath = filePath;

        this.mode = mode;

        if (Files.notExists(filePath)) {
            System.err.println(ANSI_RED + "[Error] DataFile " + filePath + " does not exist" + ANSI_RESET);
            System.exit(1);
        }

        scanner = new Scanner(Files.newBufferedReader(filePath));
        outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath.toFile(), mode));
    }

    public FileHandler(Path filePath) throws IOException {
        this(filePath, true);
    }

    public String read() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            
        }
        return null;

    }

    public String readLastString() {
        try {
            String lastString = null;
            while (scannerHasNext()) {
                lastString = read();
            }
            return lastString;
        } catch (NoSuchElementException e) {
            
        }
        return null;
    }

    public void write(String writeableString) {
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath.toFile(), mode))) {
            outputStreamWriter.write(writeableString);
            outputStreamWriter.flush();
        } catch (IOException e) {
            System.err.println("[Error] String is not written to the file: " + filePath.getFileName());
            System.err.println(e.getMessage());
        }
    }

    public boolean scannerHasNext() {
        return scanner.hasNextLine();
    }
}
