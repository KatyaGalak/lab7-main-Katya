package lab6.client.network;

import lab6.shared.io.console.StandartConsole;

public class SharedConsoleClient extends StandartConsole {
    private final SharedClient client;
    private final StandartConsole localConsole;

    public SharedConsoleClient(SharedClient client) {
        this.client = client;
        this.localConsole = new StandartConsole();
    }

    @Override
    public void write(String message) {
        localConsole.write(message);
    }

    @Override
    public void writeln(String message) {
        localConsole.writeln(message);
    }

    // @Override
    public String read(String prompt) {
        // localConsole.write(prompt);
        // String input = localConsole.read();
        // client.sendInputResponse(input);
        return localConsole.read(prompt);
    }

    public SharedClient getClient() {
        return client;
    }


}