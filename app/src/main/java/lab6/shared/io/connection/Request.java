package lab6.shared.io.connection;

import java.io.Serializable;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
/**
 * The Request class represents a user command request in the application. It encapsulates
 * the command string, its associated arguments, and a console instance for output.
 */
public class Request implements Serializable {

    @Getter
    private String command;

    @Getter
    private List<String> args;

    @Getter
    private Mark mark;

    @Getter
    private UserCredentials userCredentials;

    //@Getter
    //private UserCredentials userCredentials;

    /**
     * Constructs a new Request with the specified command, arguments, and console.
     *
     * @param command the command string
     * @param args the list of arguments associated with the command
     */
    public Request(final String command, final List<String> args, UserCredentials userCredentials) {
        this.command = command;
        this.args = args;

        this.userCredentials = userCredentials;
    }

    public Request(Mark mark, String command, List<String> input, UserCredentials userCredentials) {
        this.mark = mark;
        args = input;
        this.command = command;

        this.userCredentials = userCredentials;
    }

    /**
     * Constructs a new Request with the specified command and console.
     *
     * @param command the command string
     */
    public Request(final String command, UserCredentials userCredentials) {

        this.command = command;

        this.userCredentials = userCredentials;
    }

}
