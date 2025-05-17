package lab6.shared.ticket;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"x", "y"})
/**
 * The Coordinates class represents a point in a two-dimensional space.
 * It contains x and y coordinates and provides methods for validation,
 * comparison, and string representation.
 */
public class Coordinates implements Comparable<Coordinates>, Serializable {
    private double x;
    private float y; // Значение поля должно быть больше -11

    public void validate() throws IllegalArgumentException {
        if (y <= -11) throw new IllegalArgumentException("y must be > -11");
    }

    /**
     * Constructs a Coordinates object with the specified x and y values.
     * 
     * @param x the x coordinate
     * @param y the y coordinate, must be greater than -11
     */
    public Coordinates(double x, float y) {

        this.x = x;
        this.y = y;
    }

    /**
     * Default constructor for the Coordinates class.
     * Initializes the coordinates to default values.
     */
    public Coordinates() {}


    /**
     * Returns the x coordinate.
     * 
     * @return the x coordinate
     */
    public double getX() {

        return x;
    }

    /**
     * Returns the y coordinate.
     * 
     * @return the y coordinate
     */
    public float getY() {

        return y;
    }

    /**
     * Sets the x coordinate to the specified value.
     * 
     * @param x the new x coordinate
     */
    public void setX(double x) {

        this.x = x;
    }

    /**
     * Sets the y coordinate to the specified value.
     * 
     * @param y the new y coordinate, must be greater than -11
     */
    public void setY(float y) {

        this.y = y;
    }

    /**
     * Compares this Coordinates object with another Coordinates object.
     * 
     * @param coordinates the Coordinates object to compare with
     * @return a negative integer, zero, or a positive integer as this
     *         Coordinates is less than, equal to, or greater than the
     *         specified Coordinates
     */
    @Override
    public int compareTo(Coordinates coordinates) {

        if (coordinates == null) return 1;

        if (Double.compare(this.x, coordinates.x) == 0)
            return Double.compare(this.x, coordinates.x);

        return Float.compare(this.y, coordinates.y);
    }

    /**
     * Returns a string representation of the Coordinates object.
     * 
     * @return a string in the format "Coordinates [ x = x_value, y = y_value ]"
     */
    @Override
    public String toString() {

        return "Coordinates [" +
                    "\n\t\t x = " + x +
                    "\n\t\t y = " + y + " \n\t ]";

    }
}
