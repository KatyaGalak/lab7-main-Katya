package lab6.server.system.usersRequest;

import java.util.function.Predicate;

import lab6.server.SharedConsoleServer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The Request class is an abstract class that serves as a base for handling user requests
 * for various types of objects. It provides methods for prompting the user for input and
 * validating that input based on specified criteria.
 *
 * @param <T> the type of object that this request will create
 */
public class ClientRequest<T> {
    private SharedConsoleServer console;

    /**
     * Constructs a new Request with the specified console for user interaction.
     *
     * @param console the console instance used for input and output
     */
    public ClientRequest(SharedConsoleServer console) {

        this.console = console;
    }

    // /**
    //  * Creates an object of type T based on user input.
    //  *
    //  * @return an object of type T created from user input
    //  */
    // public abstract T create();


    /**
     * Prompts the user for a numeric value of the specified type and validates it.
     *
     * @param name the name of the value being requested
     * @param limitations any limitations on the value
     * @param predicate a predicate to validate the input
     * @param type the class type of the numeric value
     * @param <S> the type of the numeric value
     * @return the validated numeric value
     */
    public <S extends Number> S askNumericValue(String name, String limitations, Predicate<S> predicate, Class<S> type) {
        StringBuilder promt = new StringBuilder("");
        while (true) {
            if (!console.isFileScanner()) {
                promt.append("  # Enter " + name + (limitations == null ? "" : ("; field restrictions: " + limitations)) + " :");
            }
            
            String input = console.read(promt.toString());

            Number val = null;
            if (input != null && !input.isEmpty()) {
                input = input.trim();
                try {
                    if (type == Integer.class) {
                        val = Integer.valueOf(input);
                    } else if (type == Long.class) {
                        val = Long.valueOf(input);
                    } else if (type == Float.class) {
                        val = Float.valueOf(input);
                    } else if (type == Double.class) {
                        val = Double.valueOf(input);
                    }
                } catch (NumberFormatException e) {
                    if (!console.isFileScanner())
                        promt = new StringBuilder("  # Incorrect data for " + name + ". Please enter a valid number. Try again.");
                    else
                        return null;
                    continue;
                }
            }

            if (val == null && predicate.test(null)) {
                return null;
            } else if (val == null) {
                if (!console.isFileScanner())
                    promt = new StringBuilder("  # Incorrect data for " + name + ". Value cannot be null. Try again.");
                else
                    return null;
                continue;
            }

            if (predicate.test(type.cast(val))) {
                return type.cast(val);
            }

            if (!console.isFileScanner())
                promt = new StringBuilder("  # Incorrect data for " + name + "; make sure that the entered data meets the restrictions: " 
                                + limitations + "\n   # and try again.");
            else   
                return null;
        
        }
    }

    /**
     * Prompts the user for a string value and validates it.
     *
     * @param name the name of the value being requested
     * @param limitations any limitations on the value
     * @param predicate a predicate to validate the input
     * @return the validated string value
     */
    public String askString(String name, String limitations, Predicate<String> predicate) {
        StringBuilder promt = new StringBuilder("");
        while (true) {
            if (!console.isFileScanner())
                promt.append("  # Enter " + name + (limitations == null ? "" : ("; field restrictions: " + limitations)) + " :");

            String input = console.read(promt.toString());
            if (input != null)
                input = input.trim();

            if (input == "" && predicate.test(""))
                return null;
            
            if (predicate.test(input))
                return input;
            else {
                if (!console.isFileScanner())
                    promt = new StringBuilder("  # Incorrect data for " + name + "; make sure that the entered data meets the restrictions: " + limitations + "\n and try again.");
                else return null;
            }
        }
    }

    /**
     * Prompts the user for a boolean value and validates it.
     *
     * @param name the name of the value being requested
     * @param limitations any limitations on the value
     * @param predicate a predicate to validate the input
     * @return the validated boolean value
     */
    public Boolean askBoolean(String name, String limitations, Predicate<String> predicate) {
        StringBuilder promt = new StringBuilder("");
        while (true) {
            if (!console.isFileScanner())
                promt.append("  # Enter " + name + (limitations == null ? "" : ("; field restrictions: " + limitations)) + " :");
            
            String input = console.read(promt.toString());
            if (input != null) {
                input = input.trim();
            }
            Boolean booleanInput = null;

            if (input == null && predicate.test(null)
                || (input != null && input.isEmpty() && predicate.test("")))
                return null;
            else if (input == null || input.isEmpty()) {
                if (!console.isFileScanner())
                    promt = new StringBuilder("  # Incorrect data for " + name + ". Value cannot be null. Try again.");
                else
                    return null;
                continue;
            }
            
            if (input.equalsIgnoreCase("TRUE"))
                booleanInput = true;
            else if (input.equalsIgnoreCase("FALSE"))
                booleanInput = false;
            if (booleanInput != null && predicate.test(input))
                return booleanInput;
            else {
                if (!console.isFileScanner())
                    promt = new StringBuilder("  # Incorrect data for " + name + (limitations == null ? "" 
                        : "; make sure that the entered data meets the restrictions: " + limitations + "\n and") + " try again.");
                else
                    return null;
            }
        }
    }

    /**
     * Prompts the user for a LocalDateTime value and validates it.
     *
     * @param name the name of the value being requested
     * @param limitations any limitations on the value
     * @param predicate a predicate to validate the input
     * @return the validated LocalDateTime value
     */
    public LocalDateTime askLocalDateTime(String name, String limitations, Predicate<LocalDateTime> predicate) {
        StringBuilder promt = new StringBuilder("");
        while (true) {
            if (!console.isFileScanner())
                promt.append("  # Enter the date of " + name + " In the format 'dd-MM-yyyy HH:mm:ss' " + (limitations == null ? "" : ("; field restrictions: " + limitations)) + ":");
            
            String input = console.read(promt.toString());
            if (input != null && input.trim().isEmpty())
                input = input.trim();

            if (input == null && predicate.test(null))
                return null;
            else if (input == null) {
                if (!console.isFileScanner())
                    promt = new StringBuilder("  # Incorrect data for " + name + ". Value cannot be null. Try again.");
                else
                    return null;
                continue;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            try {
                LocalDateTime date = LocalDateTime.parse(input, formatter);
                if (predicate.test(date))
                    return date;
                else {
                    if (!console.isFileScanner())
                        promt = new StringBuilder("  # Incorrect data for " + name + "; make sure that the entered data meets the restrictions: " + limitations 
                                        + "\n and matches the format 'dd-MM-yyyy HH:mm:ss' " + "\n and try again.");
                    else
                        return null;
                }
            } catch (DateTimeParseException e) {
                if (!console.isFileScanner())
                    promt = new StringBuilder("  # Incorrect data for " + name + ". Please enter a valid Date. Try again.");
                else
                    return null;
                continue;
            }
        }
    }

    /**
     * Prompts the user for an enum value and validates it.
     *
     * @param name the name of the value being requested
     * @param enumClass the class of the enum type
     * @param predicate a predicate to validate the input
     * @param <E> the type of the enum
     * @return the validated enum value
     */
    public <E extends Enum<E>> E askEnum(String name, Class<E> enumClass, Predicate<E> predicate) {
        StringBuilder promt = new StringBuilder("");
        if (!console.isFileScanner())
                promt.append("  # Enter " + name + " :");
        E[] availableValues = enumClass.getEnumConstants();
        while (true) {
            if (!console.isFileScanner()) {
                promt.append("  # Available values for " + name + ":");

                for (var val : availableValues) {
                    promt.append("\t" + val.toString() + ';');
                }
            }
            
            String input = console.read(promt.toString());
            if (input != null && !input.trim().isEmpty()) {
                input = input.trim();

                for (var val : availableValues) {
                    if (val.name().equals(input.toUpperCase())) {
                        return val;
                    }
                }
            }

            if ((input == null || input.isEmpty()) && !predicate.test(null)) {
                if (!console.isFileScanner())
                    promt = new StringBuilder("  # Incorrect data for " + name + ". Value cannot be null. Try again.");
                else
                    return null;
                continue;
            }
            
            if (!console.isFileScanner())
                promt = new StringBuilder("  # The value with name '" + input + "' was not found, check the spelling correctly and try again.");
            else
                return null;
        }
    }
}
