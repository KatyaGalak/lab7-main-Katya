package lab6.client;

import lab6.client.io.Handler;
import lab6.client.network.SharedClient;
import lab6.client.network.SharedConsoleClient;

public class ClientMain {
    public static void main(String[] args) {
        try {
            SharedClient client = new SharedClient();
            SharedConsoleClient console = new SharedConsoleClient(client);
            Handler handler = new Handler(console);
            handler.run();
        } catch (Exception e) {
            System.err.println("    # Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}