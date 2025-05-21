package lab6.server;

import lab6.shared.io.console.StandartConsole;

public class ServerMain {
    public static void main(String[] args) {
        StandartConsole stdConsole = new StandartConsole();

        try {
            Server server = Server.getInstance();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.shutdown();
                System.out.println("Server shut down");
            }));

            server.run();

        } catch (Exception e) {
            stdConsole.writeln(e.toString());
        }




    }
}