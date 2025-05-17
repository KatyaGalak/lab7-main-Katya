package lab6.server.system.configuration;

import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The FileConfiguration class is responsible for managing the configuration of file paths
 * used in the application. It checks the existence and accessibility of the specified files
 * and creates them if they do not exist. The class uses environment variables to determine
 * the file paths.
 */
public class FileConfiguration {

    private static final String NAME_FILE = "LAB5_DATA_FILE";

    public static final String ID_SEQ_NAME = "ID_SEQ";

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";

    public static final Path FILE_PATH = Path.of(System.getenv(NAME_FILE) == null ? "data.csv" 
                                                                                    : System.getenv(NAME_FILE));

    public static final Path ID_SEQ_PATH = Path.of(ID_SEQ_NAME);
    
    static {
        checkFile(FILE_PATH);
        checkFile(ID_SEQ_PATH);
    }

    /**
     * Checks if the specified file exists, is a regular file, and is readable. If the file
     * does not exist, it attempts to create it. If any of these checks fail, an error message
     * is printed to the console, and the application exits.
     *
     * @param filePath the path of the file to check
     */
    private static void checkFile(final Path filePath) {

        if (!Files.exists(filePath)) {
            System.err.println(ANSI_RED + "[Error] DataFile " + filePath + " does not exist" + ANSI_RESET);
            try {
                Files.createFile(filePath);
                System.out.println("Try to create file " + filePath);
            } catch (IOException e) {
                System.err.println(ANSI_RED + "[Error] Unable to create file " + filePath + ANSI_RESET);

                System.exit(1);
            }
        } else if (!Files.isRegularFile(filePath)) {
            System.err.println(ANSI_RED + "[Error] The specified path " + filePath + " is not a file" + ANSI_RESET);
            System.exit(1);
        } else if (!Files.isReadable(filePath)) {
            System.err.println(ANSI_RED + "[Error] No rights to read file " + filePath + ANSI_RESET);
            System.exit(1);
        }
    }
}
