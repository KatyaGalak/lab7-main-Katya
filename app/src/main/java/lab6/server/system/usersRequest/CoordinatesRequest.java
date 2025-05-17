package lab6.server.system.usersRequest;

import java.util.function.Predicate;

import lab6.server.SharedConsoleServer;
import lab6.shared.ticket.Coordinates;

public class CoordinatesRequest extends ClientRequest<Coordinates> {
    public CoordinatesRequest(SharedConsoleServer console) {
        super(console);
    }

    // @Override
    public Coordinates create() {
        Predicate<Double> predicateX = x -> (x != null);
        double X = askNumericValue("X coordinates", null, predicateX, Double.class);

        if (!predicateX.test(X))
            return null;

        Predicate<Float> predicateY = y -> (y != null && y > -11);
        float Y = askNumericValue("Y coordinates", "значение должно быть больше -11", predicateY, Float.class);

        if (!predicateY.test(Y))
            return null;

        return new Coordinates(X, Y);
    }
}