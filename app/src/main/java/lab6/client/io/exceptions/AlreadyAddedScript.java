package lab6.client.io.exceptions;

public class AlreadyAddedScript extends Exception {
    public AlreadyAddedScript(String nameScript) {
        super(nameScript);
    }
}   
