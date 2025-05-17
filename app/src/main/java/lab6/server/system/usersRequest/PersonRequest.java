package lab6.server.system.usersRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.function.Predicate;

import lab6.server.SharedConsoleServer;
import lab6.shared.ticket.Color;
import lab6.shared.ticket.Person;

/**
 * The PersonRequest class is responsible for handling user requests to input
 * personal information, including the person's birthday, passport ID, and hair color.
 * It extends the Request class and provides methods to create a Person object based on user input.
 */
public class PersonRequest extends ClientRequest<Person> {

    private static HashSet<String> passportIDPerson = new HashSet<>();

    public static boolean checkUnique(String passportID) {
        if (!passportIDPerson.contains(passportID)) {
            passportIDPerson.add(passportID);
            return true;
        }

        return false;
    }

    public static void deletePassportID(String passportID) {
        if (passportIDPerson.contains(passportID)) {
            passportIDPerson.remove(passportID);
        }
    }

    /**
     * Constructs a new PersonRequest with the specified console for user interaction.
     *
     * @param console the console instance used for input and output
     */
    public PersonRequest(SharedConsoleServer console) {

        super(console);
    }

    /**
     * Prompts the user to input the person's birthday, passport ID, and hair color,
     * and creates a Person object based on the provided values.
     *
     * @return a Person object containing the user-provided values, or null if the input is invalid
     */
    // @Override
    public Person create() {

        Predicate<LocalDateTime> predicateLocalDataTime = x -> (x != null);
        LocalDateTime dataTime = askLocalDateTime("Person's birthday", "The value cannot be empty", predicateLocalDataTime);

        Predicate<String> predicatePassportID = x -> (x != null && x.length() >= 5 && x.length() <= 43 && (!passportIDPerson.contains(x)));
        String passportId = askString("Person's Passport ID", "The ID must be unique, with a min length of 5 and a max length of 43", predicatePassportID);

        Predicate<Color> predicateColor = x -> (x != null);
        Color color = askColor();

        if (!predicatePassportID.test(passportId))
            return null;

        if (!predicateLocalDataTime.test(dataTime))
            return null;

        if (!predicateColor.test(color)) 
            return null;

        Person person = new Person(dataTime, passportId, color);

        return person;
    }

    /**
     * Prompts the user to input the hair color of the person.
     *
     * @return the Color object representing the person's hair color, or null if the input is invalid
     */
    private Color askColor() {

        Predicate<Color> predicateColor = x -> (x != null);
        Color color = askEnum("Person hair color", Color.class, predicateColor);

        if (!predicateColor.test(color))
            return null;

        return color;
    }
}
