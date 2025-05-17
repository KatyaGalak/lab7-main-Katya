package lab6.server;

import lab6.shared.io.console.StandartConsole;

public class ServerMain {
    public static void main(String[] args) {
        StandartConsole stdConsole = new StandartConsole();

        try {
            Server server = new Server();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.shutdown();
                System.out.println("Server shut down");
            }));

            server.run();

            // NetworkServer server = new NetworkServer();
            // SharedConsoleServer console = new SharedConsoleServer(server);
            // // Router router = new Router(console); ????

            // Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //     server.shutdown();
            //     System.out.println("Server shut down");
            // }));

            // server.run(console);

            //NetworkServer server = console.getNetwork();

            // while (true) {
            //     try {

            //         Request request = server.recive();
            //         if (request != null) {
            //             stdConsole.writeln("Routing request");
            //             Response response = router.route(request);
            //             server.send(response);

            //         }

            //     } catch (Exception e) {
            //         System.err.println("Server error: " + e.getMessage());
            //         e.printStackTrace();
            //     }
            // }
        } catch (Exception e) {
            stdConsole.writeln(e.toString());
        }




    }
}