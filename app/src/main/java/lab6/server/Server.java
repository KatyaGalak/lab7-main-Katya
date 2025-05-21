package lab6.server;

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
        //logger.info("[CHECK INTERACTIVE COMMAND] IN COMPLETE INTERACTIVE");
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
            logger.info("[SERVER] (FINISH INTERACTIVE COMMAND) Completed interactive mode for client: " + clientAddress);
        }
    }

    public void run() {
        try {
            server = new NetworkServer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[SERVER] Failed to start NetworkServer: ", e);
            return;
        }

        for (int i = 0; i < NUM_READ_THREAD_POOL; ++i) {
            readThreadPool.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        ServerRequest request = server.receive();
                        if (request == null || request.getRequest() == null) {
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
                                logger.info("[SERVER] Created new console for client: " + addr);
                                return newConsole;
                            });
                        }

                        synchronized (clientRequestQueues) { // создает очередь для клиента
                            clientRequestQueues.put(clientAddress, new LinkedBlockingQueue<>());
                        }
                        
                        
                        logger.info("[THREAD] Received request: " + request.getRequest() + " from " + clientAddress + " " + Thread.currentThread().getName());
                        
                        logger.info("[THREAD] choosing thread console " + console.toString()+ " " + Thread.currentThread().getName());

                        boolean isInteractiveCommand = request.getRequest().getCommand().equals("show");
                        if (isInteractiveCommand) {
                            synchronized (interactiveClients) {
                                interactiveClients.add(clientAddress);
                                logger.info("Started interactive mode for client: " + clientAddress);
                            }
                        }

                        // Создаём новый поток для обработки запроса
                        new Thread(() -> {
                            logger.info("[SERVER] Routing request: " + request.getRequest() + " in thread: " + Thread.currentThread().getName());
                            try {
                                Router router = new Router(console, this);
                                Response response = router.route(request.getRequest());
                                logger.info("[SERVER] Response routed and done: " + response);
                                if (response != null) {
                                    // Создаём новый поток для отправки ответа
                                    new Thread(() -> {
                                        logger.info("[SERVER] Sending response: " + response + " to " + clientAddress);
                                        server.send(response, clientAddress);

                                        
                                        logger.info("[CHECK INTERACTIVE COMMAND] DELETE INTERACTIVE COMMAND");
                                        if (isInteractiveCommand && response.getMessage() != null && response.getMark() == Mark.COMPLETED_SHOW) {
                                                //logger.info("[CHECK INTERACTIVE COMMAND] IN IF");
                                                completeInteractive(clientAddress);
                                        } else {
                                            logger.info("[SERVER] SERVER DELETE CLIENT" + clientAddress);

                                            synchronized (clientRequestQueues) {
                                                clientRequestQueues.remove(clientAddress);
                                            }

                                            synchronized (activeClients) {
                                                activeClients.remove(clientAddress);
                                            }

                                            synchronized (clientConsoles) {
                                                clientConsoles.remove(clientAddress);
                                            }
                                        }

                                        
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
