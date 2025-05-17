package lab6.shared.ticket;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lab6.server.system.usersRequest.PersonRequest;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonPropertyOrder({"birthday", "passportID", "hairColor"})
public class Person implements Comparable<Person>, Serializable {
    @JsonProperty("birthday")
    private LocalDateTime birthday; // Поле не может быть null

    @JsonProperty("passportID")
    private String passportID; // Значение этого поля должно быть уникальным, Длина строки должна быть не меньше 5, Длина строки не должна быть больше 43, Поле не может быть null
    
    @JsonProperty("hairColor")
    private Color hairColor; // Поле не может быть null

    /**
     * Constructs a Person object with the specified birthday, passport ID, and hair color.
     * 
     * @param birthday the birthday of the person, cannot be null
     * @param passportID the unique passport ID of the person, must be between 5 and 43 characters
     * @param hairColor the hair color of the person, cannot be null
     */
    public Person(LocalDateTime birthday, String passportID, Color hairColor) {

        this.birthday = birthday;
        this.passportID = passportID;
        this.hairColor = hairColor;
    }

    /**
     * Default constructor for the Person class.
     * Initializes a new Person object with default values.
     */
    public Person() {}


    /**
     * Validates the Person object to ensure all fields meet the required constraints.
     * 
     * @throws IllegalArgumentException if any field is invalid
     */
    public void validate() throws IllegalArgumentException {

        if (birthday == null) throw new IllegalArgumentException("Birthday cannot be null");

        if (hairColor == null) throw new IllegalArgumentException("HairColor cannot be null");

        if (passportID == null) throw new IllegalArgumentException("PassportID cannot be null");

        if (passportID.length() < 5 || passportID.length() > 43) throw new IllegalArgumentException("The length of the passportID does not comply with the rules");

        if (!PersonRequest.checkUnique(passportID)) throw new IllegalArgumentException("PassportID must be unique");

    }

    /**
     * Returns the birthday of the person.
     * 
     * @return the birthday of the person
     */
    public LocalDateTime getBirthday() {

        return birthday;
    }

    /**
     * Returns the passport ID of the person.
     * 
     * @return the passport ID of the person
     */
    public String getPassportID() {

        return passportID;
    }

    /**
     * Returns the hair color of the person.
     * 
     * @return the hair color of the person
     */
    public Color getHairColor() {

        return hairColor;
    }

    /**
     * Returns a hash code value for the Person object.
     * 
     * @return a hash code value for the Person object
     */
    @Override
    public int hashCode() {

        return Objects.hash(birthday, passportID, hairColor);
    }

    /**
     * Compares this Person object to the specified object for equality.
     * 
     * @param o the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return true;

        Person person = (Person) o;

        return Objects.equals(hairColor, person.hairColor) &&
                Objects.equals(passportID, person.passportID) &&
                Objects.equals(birthday, person.birthday);
    }

    /**
     * Returns a string representation of the Person object.
     * 
     * @return a string representation of the Person object
     */
    @Override
    public String toString() {

        String ans = "Person: [";

        ans += "birthday = " + birthday.toString()
                + ", passportID = " + passportID
                + ", hairColor = " + hairColor.toString()
                + "]\n";

        return ans;
    }

    /**
     * Compares this Person object with another Person object for order.
     * 
     * @param person the Person object to compare with
     * @return a negative integer, zero, or a positive integer as this
     *         Person is less than, equal to, or greater than the specified Person
     */
    @Override
    public int compareTo(Person person) {

        if (person == null) return 1;

        int ans = this.birthday.compareTo(person.birthday);

        if (ans == 0) ans = this.passportID.compareTo(person.passportID);

        if (ans == 0) ans = this.hairColor.compareTo(person.hairColor);

        return ans;
    }
}
