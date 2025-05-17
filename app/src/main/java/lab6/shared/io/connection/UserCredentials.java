package lab6.shared.io.connection;

import java.io.Serializable;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

public record UserCredentials(String username, String passwordHash) implements Serializable {
    public UserCredentials {
        if (passwordHash != null)
            passwordHash = sha1Hex(passwordHash);
    }

    public static UserCredentials getEmptyUserCredentials() {
        return new UserCredentials(null, null);
    }
}
