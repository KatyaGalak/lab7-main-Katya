package lab6.shared.ticket;

/**
 * The TicketType enum represents the various types of tickets available.
 * Each ticket type has an associated value that indicates its priority or category.
 */
public enum TicketType {
    VIP(4),
    USUAL(3),
    BUDGETARY(2),
    CHEAP(1);

    private final int value;

    /**
     * Constructs a TicketType with the specified value.
     * 
     * @param value the value associated with the ticket type
     */
    TicketType(int value) {

        this.value = value;
    }

    /**
     * Returns the value associated with the ticket type.
     * 
     * @return the value of the ticket type
     */
    public int getValue() {

        return value;
    }
}
