package lab6.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


import lab6.shared.io.connection.Response;
import lab6.server.system.collection.CollectionManager;
import lab6.shared.io.connection.Mark;

public class Server {
    private static final int NUM_READ_THREAD_POOL = 5;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private ExecutorService readThreadPool;
    private NetworkServer server;

    private static Server instance;

    private final Map<InetSocketAddress, SharedConsoleServer> clientConsoles = new ConcurrentHashMap<>(); // client - console

    private final static ConcurrentHashMap<InetSocketAddress, BlockingQueue<ServerRequest> > clientRequestQueues = new ConcurrentHashMap<>(); // очередь для request, которые пришли не туда

    private final Set<InetSocketAddress> activeClients = ConcurrentHashMap.newKeySet(); // храним активных клиентов

    private final Set<InetSocketAddress> interactiveClients = ConcurrentHashMap.newKeySet(); // Для интерактивных команд

    private Server() {
        readThreadPool = Executors.newFixedThreadPool(NUM_READ_THREAD_POOL);
    }

    public static synchronized Server getInstance() { 
        return instance == null ? instance = new Server() : instance;
    }

    public static synchronized BlockingQueue<ServerRequest> getClientRequestQueue(InetSocketAddress clientAddress) {
        return clientRequestQueues.computeIfAbsent(clientAddress, k -> new LinkedBlockingQueue<>());
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

        clientRequestQueues.clear();
        activeClients.clear();
        clientConsoles.clear(); // Очистка консолей при завершении
        interactiveClients.clear();
    }

    public void completeInteractive(InetSocketAddress clientAddress) {
        logger.info("[CHECK INTERACTIVE COMMAND] IN COMPLETE INTERACTIVE");
        synchronized (interactiveClients) {
            interactiveClients.remove(clientAddress);
            synchronized (clientRequestQueues) {
                clientRequestQueues.remove(clientAddress);
            }
            synchronized (activeClients) {
                activeClients.remove(clientAddress);
            }
            synchronized (clientConsoles) {
                clientConsoles.remove(clientAddress);
            }
            logger.info("[FINISH INTERACTIVE COMMAND] Completed interactive mode for client: " + clientAddress);
        }
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
                        logger.info("[THREAD] Received request: " + request.getRequest() + " from " + clientAddress + " " + Thread.currentThread().getName());

                        // проверка на интерактивность запроса
                        boolean isInteractiveRequest = request.getRequest().getMark() != null &&
                            (request.getRequest().getMark().equals(Mark.INPUT_RESPONCE) ||
                             request.getRequest().getMark().equals(Mark.WAIT_NEXT));
                        
                        synchronized (activeClients) {
                            if (activeClients.contains(clientAddress)) {

                                if (isInteractiveRequest) {
                                    SharedConsoleServer interactiveConsole = clientConsoles.get(clientAddress);

                                    if (interactiveConsole != null) {
                                        clientRequestQueues.computeIfAbsent(clientAddress, k -> new LinkedBlockingQueue<>()).add(request);
                                    }
                                }

                                
                                continue;
                            }
                            

                            activeClients.add(clientAddress);
                        }

                        SharedConsoleServer console;

                        synchronized (clientConsoles) {
                            // Получаем или создаём консоль для клиента
                            console = clientConsoles.computeIfAbsent(clientAddress, addr -> {
                                SharedConsoleServer newConsole = new SharedConsoleServer(server);
                                newConsole.setClientAddress(addr);
                                logger.info("Created new console for client: " + addr);
                                return newConsole;
                            });
                        }

                        synchronized (clientRequestQueues) { // создает очередь для клиента
                            clientRequestQueues.put(clientAddress, new LinkedBlockingQueue<>());
                        }
                        
                        
                        logger.info("[THREAD] Received request: " + request.getRequest() + " from " + clientAddress + " " + Thread.currentThread().getName());

                        // set обрабатываемых клиентов

                        
                        logger.info("[THREAD] choosing thread console " + console.toString()+ " " + Thread.currentThread().getName());

                        // Помещаем INPUT_RESPONCE и WAIT_NEXT в очередь консоли
                        // console.offerResponse(request);

                        

                        // Пропускаем маршрутизацию для INPUT_RESPONCE и WAIT_NEXT
                        // if (request.getRequest().getMark() != null && 
                        //     (request.getRequest().getMark().equals(Mark.INPUT_RESPONCE) || 
                        //      request.getRequest().getMark().equals(Mark.WAIT_NEXT))) {

                        //     console.offerRequest(request); // ????!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! должно ли вообще это быть здесь
                            
                        //     logger.info("Skipping routing for " + request.getRequest().getMark() + " from " + clientAddress);
                        //     continue;
                        // }

                        boolean isInteractiveCommand = request.getRequest().getCommand().equals("show");
                        if (isInteractiveCommand) {
                            synchronized (interactiveClients) {
                                interactiveClients.add(clientAddress);
                                logger.info("Started interactive mode for client: " + clientAddress);
                            }
                        }

                        // Создаём новый поток для обработки запроса
                        new Thread(() -> {
                            logger.info("Routing request: " + request.getRequest() + " in thread: " + Thread.currentThread().getName());
                            try {
                                Router router = new Router(console, this);
                                Response response = router.route(request.getRequest());
                                logger.info("Response routed and done: " + response);
                                if (response != null) {
                                    // Создаём новый поток для отправки ответа
                                    new Thread(() -> {
                                        logger.info("Sending response: " + response + " to " + clientAddress);
                                        server.send(response, clientAddress);

                                        

                                        logger.info("[CHECK INTERACTIVE COMMAND] DELETE INTERACTIVE COMMAND?");
                                        if (isInteractiveCommand && response.getMessage() != null &&
                                            (response.getMessage().equals("Show completed") ||
                                             response.getMessage().equals("Show terminated by client") ||
                                             response.getMessage().equals("Collection is empty"))) {
                                                logger.info("[CHECK INTERACTIVE COMMAND] IN IF");
                                                completeInteractive(clientAddress);
                                        } else {
                                            logger.info("[!!!!!SERVER DELETE CLIENT]" + clientAddress);

                                            // synchronized (clientRequestQueues) {
                                            //     clientRequestQueues.remove(clientAddress);
                                            // }

                                            // synchronized (activeClients) {
                                            //     activeClients.remove(clientAddress);
                                            // }

                                            // synchronized (clientConsoles) {
                                            //     clientConsoles.remove(clientAddress);
                                            // }

                                            synchronized (interactiveClients) {
                                                if (!interactiveClients.contains(clientAddress) && !isInteractiveCommand) {
                                                    synchronized (clientRequestQueues) {
                                                        clientRequestQueues.remove(clientAddress);
                                                    }
                                                    synchronized (activeClients) {
                                                        activeClients.remove(clientAddress);
                                                    }
                                                    synchronized (clientConsoles) {
                                                        clientConsoles.remove(clientAddress);
                                                    }
                                                    logger.info("Removed client: " + clientAddress);
                                                }
                                            }
                                        }

                                        
                                    }).start();
                                }
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Error processing request: " + request.getRequest(), e);
                            } finally {
                                // logger.info("[!!!!!SERVER DELETE CLIENT]" + clientAddress);

                                // // synchronized (clientRequestQueues) {
                                // //     clientRequestQueues.remove(clientAddress);
                                // // }

                                // // synchronized (activeClients) {
                                // //     activeClients.remove(clientAddress);
                                // // }

                                // // synchronized (clientConsoles) {
                                // //     clientConsoles.remove(clientAddress);
                                // // }

                                // synchronized (interactiveClients) {
                                //     if (!interactiveClients.contains(clientAddress) && !isInteractiveCommand) {
                                //         synchronized (clientRequestQueues) {
                                //             clientRequestQueues.remove(clientAddress);
                                //         }
                                //         synchronized (activeClients) {
                                //             activeClients.remove(clientAddress);
                                //         }
                                //         synchronized (clientConsoles) {
                                //             clientConsoles.remove(clientAddress);
                                //         }
                                //         logger.info("Removed client: " + clientAddress);
                                //     }
                                // }
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
