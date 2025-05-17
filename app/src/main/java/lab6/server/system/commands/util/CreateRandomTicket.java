package lab6.server.system.commands.util;

//import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import lab6.server.system.usersRequest.PersonRequest;
import lab6.shared.ticket.Color;
import lab6.shared.ticket.Coordinates;
//import lab6.shared.ticket.IDGenerator;
import lab6.shared.ticket.Person;
import lab6.shared.ticket.Ticket;
import lab6.shared.ticket.TicketType;

import java.time.ZoneOffset;

/**
 * Utility class for generating random Ticket objects.
 */
public class CreateRandomTicket {
    private static final int MIN_LENGTH_NAME = 5;
    private static final int MAX_LENGTH_NAME = 10;

    private static final int MIN_LENGTH_PASSPORTID = 5;
    private static final int MAX_LENGTH_PASSPORTID = 40;
    private static final int MAX_NUMBER_PASSPORTID = 9;

    private static final int ASCII_START = 97;
    private static final int ASCII_END = 122;

    private static final double MIN_DOUBLE = -8;
    private static final double MAX_DOUBLE = 2000;

    private static final float MIN_FLOAT = -10;
    private static final float MAX_FLOAT = 4300;

    private static final double MIN_DOUBLE_PRICE = 100;
    private static final double MAX_DOUBLE_PRICE = 20000;

    private static final LocalDateTime START_TIME = LocalDateTime.of(2005, 1, 1, 10, 0, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2023, 12, 31, 10, 0, 0);

    /**
     * Generates a random passport ID.
     *
     * @return A unique random passport ID.
     */
    private static String getRandomPassportID() {
        while (true) {
            SecureRandom random = new SecureRandom();
            int sizePassportID = random.nextInt(MAX_LENGTH_PASSPORTID - MIN_LENGTH_PASSPORTID + 1) + MIN_LENGTH_PASSPORTID;

            StringBuilder passportID = new StringBuilder(sizePassportID);

            for (int i = 0; i < sizePassportID; ++i) {
                passportID.append(random.nextInt(MAX_NUMBER_PASSPORTID));
            }

            if (PersonRequest.checkUnique(passportID.toString())) {
                PersonRequest.deletePassportID(passportID.toString());
                return passportID.toString();
            }
        }
    }

    /**
     * Generates a random name.
     *
     * @return A randomly generated name.
     */
    private static String getRandomName() {
        SecureRandom random = new SecureRandom();
        int sizeName = random.nextInt(MAX_LENGTH_NAME - MIN_LENGTH_NAME + 1) + MIN_LENGTH_NAME;
        
        StringBuilder name = new StringBuilder(sizeName);

        for (int i = 0; i < sizeName; ++i) {
            int asciiCode = random.nextInt(ASCII_END - ASCII_START + 1) + ASCII_START;
            name.append((char) asciiCode);
        }

        String stringName = name.toString();

        if (stringName != null && !stringName.isEmpty()) {
            stringName = stringName.substring(0, 1).toUpperCase() + stringName.substring(1);
        }

        return stringName;
    }

    /**
     * Returns a random enum value from the specified enum class.
     *
     * @param enumClass The class of the enum to choose from.
     * @param <T> The type of the enum.
     * @return A random enum value.
     */
    private static <T extends Enum<T>> T getRandomEnum(Class<T> enumClass) {
        Random random = new Random();

        int randomIndex = random.nextInt(enumClass.getEnumConstants().length);

        return enumClass.getEnumConstants()[randomIndex];
    }

    /**
     * Generates a random LocalDateTime within the specified range.
     *
     * @return A random LocalDateTime.
     */
    private static LocalDateTime getRandomLocalDataTime() {
        long startEpoch = START_TIME.toEpochSecond(ZoneOffset.UTC);
        long endEpoch = END_TIME.toEpochSecond(ZoneOffset.UTC);

        Random random = new Random();

        long randomEpoch = startEpoch + (long) (random.nextDouble() * (endEpoch - startEpoch));

        return LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneOffset.UTC);
    }

    /**
     * Generates a list of random Ticket objects.
     *
     * @param cnt The number of random tickets to generate.
     * @return A list of randomly generated Ticket objects.
     * @throws IllegalArgumentException if the generated data is invalid.
     */
    public static List<Ticket> generateRandomTicket(int cnt) throws IllegalArgumentException {
        List<Ticket> tickets = new LinkedList<>();

        Random random = new Random();

        for (int i = 0; i < cnt; ++i) {
            String name = getRandomName();

            double x = MIN_DOUBLE + (MAX_DOUBLE - MIN_DOUBLE) * random.nextDouble();
            float y = MIN_FLOAT + (MAX_FLOAT - MIN_FLOAT) * random.nextFloat();

            Coordinates coordinates = new Coordinates(x, y);

            Double price = MIN_DOUBLE_PRICE + (MAX_DOUBLE_PRICE - MIN_DOUBLE_PRICE) * random.nextDouble();

            Boolean refubdable = random.nextBoolean();

            TicketType type = getRandomEnum(TicketType.class);

            LocalDateTime birthday = getRandomLocalDataTime();

            String passportID = getRandomPassportID();

            Color hairColor = getRandomEnum(Color.class);

            Person person = new Person(birthday, passportID, hairColor);

            try {
                Ticket ticket = new Ticket(name, coordinates, price, refubdable, type, person);
                tickets.add(ticket);
            } catch (IllegalArgumentException e) {
                System.err.println("Generated Ticket has invalid data. " + e.getMessage());
                // try {
                //     IDGenerator.getInstance().deleteLastID();
                // } catch (IOException e1) {
                //     // Handle exception
                // }
                
                throw new IllegalArgumentException();
            }
        }

        return tickets;
    }
}
