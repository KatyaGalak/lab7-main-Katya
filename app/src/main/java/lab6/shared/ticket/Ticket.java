package lab6.shared.ticket;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.NonNull;
import lombok.Data;
import lombok.Setter;
import lombok.AccessLevel;

/**
 * The Ticket class represents a ticket with various attributes such as name, coordinates, price, 
 * refundable status, type, and associated person. It implements the Comparable interface to allow 
 * sorting based on ticket attributes.
 */
@Data
@JsonPropertyOrder({"name", "coordinates", "price", "refundable", "type", "person"})
public class Ticket implements Comparable<Ticket>, Serializable {
    
    @NonNull
    private String name; // Поле не может быть null, Строка не может быть пустой
    
    @JsonUnwrapped
    @NonNull
    private Coordinates coordinates; // Поле не может быть null
    
    @JsonIgnore
    @NonNull
    @Setter(AccessLevel.NONE)
    private LocalDateTime creationDate; // Поле не может быть null, Значение этого поля должно генерироваться автоматически
    
    private double price; // Значение поля должно быть больше 0
    
    private boolean refundable;

    public boolean getRefundable() {
        return refundable;
    }
    
    @NonNull
    private TicketType type; // Поле не может быть null
    
    @JsonUnwrapped
    @NonNull
    private Person person; // Поле не может быть null

    //@Setter(AccessLevel.NONE)
    @JsonIgnore
    private Long id; // Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    
    @JsonIgnore
    private Boolean installedPrice = false;
    @JsonIgnore
    private Boolean installedRefundable = false;

    /*{
        try {
            id = IDGenerator.getInstance().generateId();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            id = -1;
        }
    }*/

    private Long creatorId;

    /**
     * Default constructor for the Ticket class.
     * Initializes a new Ticket object with the current creation date.
     */
    public Ticket() {

        this.creationDate = LocalDateTime.now();
    }

    public void validate() throws IllegalArgumentException {

        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty");

        if (installedPrice && price <= 0) throw new IllegalArgumentException("Price must be >= 0");

        if (coordinates == null) throw new IllegalArgumentException("Coordinates cannot be null");

        if (type == null) throw new IllegalArgumentException("Type cannot be null");

        if (person == null) throw new IllegalArgumentException("Person cannot be null");

        person.validate();

        //if (id <= 0) throw new IllegalArgumentException("ID must be >= 0");
    }

    /**
     * Constructs a Ticket object with the specified parameters.
     * 
     * @param name the name of the ticket
     * @param coordinates the coordinates associated with the ticket
     * @param price the price of the ticket
     * @param refundable indicates if the ticket is refundable
     * @param type the type of the ticket
     * @param person the person associated with the ticket
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Ticket(String name, Coordinates coordinates, 

                    Double price, Boolean refundable, TicketType type, Person person) throws IllegalArgumentException {
        this();
        this.name = name;
        this.coordinates = coordinates;

        if (price != null)
            installedPrice = true;
        this.price = (price == null ? Integer.MIN_VALUE : price);
        
        if (refundable != null)
            installedRefundable = true;
        this.refundable = (refundable == null ? false : refundable);

        this.type = type;
        this.person = person;

        validate();
    }

    public Ticket(String name, Coordinates coordinates, 

                    Double price, Boolean refundable, TicketType type, Person person, LocalDateTime creationTime) throws IllegalArgumentException {
        this.name = name;
        this.coordinates = coordinates;

        if (price != null)
            installedPrice = true;
        this.price = (price == null ? Integer.MIN_VALUE : price);
        
        if (refundable != null)
            installedRefundable = true;
        this.refundable = (refundable == null ? false : refundable);

        this.type = type;
        this.person = person;

        this.creationDate = creationTime;

        validate();
    }

    /**
     * Sets the ID for the ticket and updates the ID generator.
     * 
     * @param id the unique ID to set for the ticket
     * @throws IllegalArgumentException if the ID is invalid
     * @throws IOException if an I/O error occurs
     */
   /* public void setId(Long id) throws IllegalArgumentException, IOException {

        try {
            if (id < 0)
                throw new IllegalArgumentException("ID must be >= 0");
    
            IDGenerator.getInstance().addId(id);
            IDGenerator.getInstance().deleteLastID();
        } catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
        }
        

        this.id = id;
    }*/

    @Override
    public int compareTo(Ticket ticket) {
        if (ticket == null) return 1;

        int ans = this.name.compareTo(ticket.name);

        if (ans == 0)
            ans = this.person.compareTo(ticket.person);
        
        if (ans == 0) 
            ans = this.creationDate.compareTo(ticket.creationDate);

        if (installedPrice && ans == 0)
            ans = Double.compare(price, ticket.price);         

        if (ans == 0) ans = this.type.compareTo(ticket.type);

        if (ans == 0) ans = this.coordinates.compareTo(ticket.coordinates);
        
        if (ans == 0) ans = Double.compare(price, ticket.price);

        if (installedRefundable && ans == 0)
            ans = Boolean.compare(refundable, ticket.refundable);

        if (ans == 0)
            ans = Long.compare(id, ticket.id);

        return ans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Ticket))
            return false;

        Ticket t = (Ticket) o;

        return Objects.equals(name, t.name) && Objects.equals(id, t.id) &&
            Objects.equals(coordinates, t.coordinates) & Objects.equals(creationDate, t.creationDate) &&
            Objects.equals(price, t.price) && Objects.equals(refundable, t.refundable) && 
            Objects.equals(type, t.type) && Objects.equals(person, t.person);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, coordinates, creationDate, person, type, price, refundable);
    }

    /**
     * Returns a string representation of the Ticket object.
     * 
     * @return a string representation of the Ticket object
     */
    @Override
    public String toString() {

        return "Ticket {" +
                "\n\t id = " + id +
                "\n\t name = " + name +
                "\n\t" + coordinates +
                "\n\t creationData = " + creationDate +
                (installedPrice ? "\n\t price = " + price : "") +
                (installedRefundable ? "\n\t refundable = " + refundable : "") +
                "\n\t type = " + type +
                "\n\t" + person + "\n}";
    }

}
