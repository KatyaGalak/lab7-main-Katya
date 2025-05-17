package lab6.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import lab6.shared.io.connection.Response;
import lab6.shared.io.connection.Mark;

public class Server {
    private static final int NUM_READ_THREAD_POOL = 5;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private ExecutorService readThreadPool;
    private NetworkServer server;
    private final Map<InetSocketAddress, SharedConsoleServer> clientConsoles = new ConcurrentHashMap<>();

    public Server() {
        readThreadPool = Executors.newFixedThreadPool(NUM_READ_THREAD_POOL);
    }

    public void shutdown() {
        logger.info("[SERVER SHUTDOWN] Shutting down thread pool and channel");
        readThreadPool.shutdown();
        try {
            if (!readThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                readThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            readThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        server.shutdown();
        clientConsoles.clear(); // Очистка консолей при завершении
    }

    public void run() {
        try {
            server = new NetworkServer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start NetworkServer: ", e);
            return;
        }

        for (int i = 0; i < NUM_READ_THREAD_POOL; ++i) {
            readThreadPool.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        //logger.info("Waiting for request in thread: " + Thread.currentThread().getName());
                        ServerRequest request = server.receive();
                        if (request == null || request.getRequest() == null) {
                            //logger.info("Received null request, continuing");
                            continue;
                        }

                        InetSocketAddress clientAddress = request.getClientAddress();
                        logger.info("[THREAD] Received request: " + request.getRequest() + " from " + clientAddress+" "+Thread.currentThread().getName());

                        // set обрабатываемых клиентов

                        // Получаем или создаём консоль для клиента
                        // SharedConsoleServer console = clientConsoles.computeIfAbsent(clientAddress, addr -> {
                        //     SharedConsoleServer newConsole = new SharedConsoleServer(server);
                        //     newConsole.setClientAddress(addr);
                        //     logger.info("Created new console for client: " + addr);
                        //     return newConsole;
                        // });
                        logger.info("[THREAD] choosing thread console " +console.toString()+ " "+Thread.currentThread().getName());

                        // Помещаем INPUT_RESPONCE и WAIT_NEXT в очередь консоли
                        // console.offerResponse(request);

                        // Пропускаем маршрутизацию для INPUT_RESPONCE и WAIT_NEXT
                        if (request.getRequest().getMark() != null && 
                            (request.getRequest().getMark().equals(Mark.INPUT_RESPONCE) || 
                             request.getRequest().getMark().equals(Mark.WAIT_NEXT))) {
                            console.offerResponse(request); // ????
                            logger.info("Skipping routing for " + request.getRequest().getMark() + " from " + clientAddress);
                            continue;
                        }

                        // Создаём новый поток для обработки запроса
                        new Thread(() -> {
                            logger.info("Routing request: " + request.getRequest() + " in thread: " + Thread.currentThread().getName());
                            try {
                                Router router = new Router(console);
                                Response response = router.route(request.getRequest());
                                logger.info("Response routed and done: " + response);
                                if (response != null) {
                                    // Создаём новый поток для отправки ответа
                                    new Thread(() -> {
                                        logger.info("Sending response: " + response + " to " + clientAddress);
                                        server.send(response, clientAddress);
                                    }).start();
                                }
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Error processing request: " + request.getRequest(), e);
                            }
                        }).start();
                    } catch (Exception e) {
                        if (!Thread.currentThread().isInterrupted()) {
                            logger.log(Level.SEVERE, "Request receiving error in thread: " + Thread.currentThread().getName(), e);
                        }
                    }
                }
            });
        }
    }
}


// package lab6.server;

// import java.io.IOException;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.TimeUnit;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import lab6.shared.io.connection.Response;
// import lab6.shared.io.connection.Mark;

// public class Server {
//     private static final int NUM_READ_THREAD_POOL = 5;
//     private static final Logger logger = Logger.getLogger(Server.class.getName());
//     private ExecutorService readThreadPool;
//     private NetworkServer server;

//     public Server() {
//         readThreadPool = Executors.newFixedThreadPool(NUM_READ_THREAD_POOL);
//     }

//     public void shutdown() {
//         logger.info("[SERVER SHUTDOWN] Shutting down thread pool and channel");
//         readThreadPool.shutdown();
//         try {
//             if (!readThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
//                 readThreadPool.shutdownNow();
//             }
//         } catch (InterruptedException e) {
//             readThreadPool.shutdownNow();
//             Thread.currentThread().interrupt();
//         }
//         server.shutdown();
//     }

//     public void run() {
//         try {
//             server = new NetworkServer();
//         } catch (Exception e) {
//             logger.log(Level.SEVERE, "Failed to start NetworkServer: ", e);
//             return;
//         }

//         for (int i = 0; i < NUM_READ_THREAD_POOL; ++i) {
//             readThreadPool.submit(() -> {
//                 SharedConsoleServer console = new SharedConsoleServer(server);
//                 Router router = new Router(console);
//                 while (!Thread.currentThread().isInterrupted()) {
//                     try {
//                         ServerRequest request = server.receive();
//                         if (request == null || request.getRequest() == null) {
//                             continue;
//                         }
//                         console.setClientAddress(request.getClientAddress());
//                         logger.info("Received request: " + request.getRequest() + " from " + request.getClientAddress());

//                         // Помещаем INPUT_RESPONCE и WAIT_NEXT в очередь консоли
//                         console.offerResponse(request);

//                         // Пропускаем маршрутизацию для INPUT_RESPONCE и WAIT_NEXT
//                         if (request.getRequest().getMark() != null && 
//                             (request.getRequest().getMark().equals(Mark.INPUT_RESPONCE) || 
//                              request.getRequest().getMark().equals(Mark.WAIT_NEXT))) {
//                             logger.info("Skipping routing for " + request.getRequest().getMark() + " from " + request.getClientAddress());
//                             continue;
//                         }

//                         // Создаем новый поток для обработки запроса
//                         new Thread(() -> {
//                             logger.info("Routing request!");
//                             try {
//                                 Response response = router.route(request.getRequest());
//                                 logger.info("Response routed and done");
//                                 if (response != null) {
//                                     // Создаем новый поток для отправки ответа
//                                     new Thread(() -> {
//                                         logger.info("Sending response: " + response + " to " + request.getClientAddress());
//                                         server.send(response, request.getClientAddress());
//                                     }).start();
//                                 }
//                             } catch (Exception e) {
//                                 logger.log(Level.SEVERE, "Error processing request: ", e);
//                             }
//                         }).start();
//                     } catch (Exception e) {
//                         if (!Thread.currentThread().isInterrupted()) {
//                             logger.log(Level.SEVERE, "Request receiving error: ", e);
//                         }
//                     }
//                 }
//             });
//         }
//     }
// }


// package lab6.server;

// import java.io.IOException;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.logging.Level;
// import java.util.logging.Logger;

// import lab6.shared.io.connection.Response;
// import lab6.shared.io.connection.Mark;
// import lab6.shared.io.connection.Request;

// public class Server {
//     // тут должно быть server.run();

//     private static final int NUM_READ_THREAD_POOL = 5;
//     private static final Logger logger = Logger.getLogger(Router.class.getName());

//     private ExecutorService readThreadPool;

//     NetworkServer server;

//     public Server() {
//         readThreadPool = Executors.newFixedThreadPool(NUM_READ_THREAD_POOL);
//     }

//     public void shutdown() {
//         logger.info("[SERVER SHUTDOWN] Shutting down thread pool and channel");
//         readThreadPool.shutdown();
//         server.shutdown();
//         // try {
//         //     //channel.close();
//         // } catch (IOException e) {
//         //     logger.severe("[ERROR] Failed to close channel: " + e.getMessage()); // ????? Как закрывать каналы
//         // }
//     }

//     public void run() {
//         try {
//             server = new NetworkServer();
//         }  catch (Exception e) {
//             if (!Thread.currentThread().isInterrupted()) {
//                 logger.log(Level.SEVERE, "Request processing error: ", e);
//             }
//         }


//         for (int i = 0; i < NUM_READ_THREAD_POOL; ++i) {

//             readThreadPool.submit(() -> {
//                 // NetworkServer server = new NetworkServer();
//                 SharedConsoleServer console = new SharedConsoleServer(server);

//                 Router router = new Router(console);
//                 //SharedConsoleServer currConsole = new SharedConsoleServer(this1);

//                 while (!Thread.currentThread().interrupted()) {
//                     try {
        
//                         ServerRequest request = server.receive();
//                         if (request == null)
//                             continue;
//                         console.setClientAddress(request.getClientAddress());

//                         if (request.getRequest().getMark() != null && 
//                             (request.getRequest().getMark().equals(Mark.INPUT_RESPONCE) || 
//                             request.getRequest().getMark().equals(Mark.WAIT_NEXT))) {
//                             logger.info("Skipping routing for " + request.getRequest().getMark() + " from " + request.getClientAddress());
//                             continue;
//                         }

//                         // if (request != null) {

//                         new Thread(() -> {
//                             logger.info("Routing request!");

                            
//                             try {
//                                 logger.info("Setting client addres");
//                                 //SharedConsoleServer.setClientAddress(request.getClientAddress());
//                                 logger.info("Starting executing process");
//                                 Response response = router.route(request.getRequest());
//                                 logger.info("Responce routed and done");
                                
//                                 new Thread(() -> {
//                                     //logger.info("Start inet send thread process" + request.getClientAddress());
//                                     //SharedConsoleServer.setClientAddress(request.getClientAddress());
//                                     logger.info("Sending responce " + response.toString());
//                                     //server.send(response, request.getClientAddress());
//                                     server.send(response, request.getClientAddress());
//                                 }).start();
//                             } finally {
//                                 //SharedConsoleServer.clearClientAddress();
//                             }
                            
//                         }).start();
//                         // }
        
//                     } catch (Exception e) {
//                         if (!Thread.currentThread().isInterrupted()) {
//                             logger.log(Level.SEVERE, "Request processing error: ", e);
//                         }
//                     }
//                 }
//             });
//         }
//     }
// }
