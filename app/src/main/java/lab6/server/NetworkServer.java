package lab6.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;

public class NetworkServer {
    private static final int PORT = 2223;
    private static final int BUFFER_SIZE = 65535;
    private DatagramChannel channel;
    //InetSocketAddress clientAddress;
   // private ThreadLocal<InetSocketAddress> clientAddress = new ThreadLocal<>();
    //private InetSocketAddress lastClientAddress;
    //private final Object addressLock = new Object();

    private static final Logger logger = Logger.getLogger(NetworkServer.class.getName());

    //private ExecutorService readThreadPool;

    //private static final int NUM_READ_THREAD_POOL = 5;
    
    //SharedConsoleServer console;
    //Router router;

    public NetworkServer() throws IOException {
        channel = DatagramChannel.open();
        channel.socket().setReuseAddress(true);
        channel.configureBlocking(false);
        //readThreadPool = Executors.newFixedThreadPool(NUM_READ_THREAD_POOL);
        try {
            channel.bind(new InetSocketAddress(PORT));
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        logger.info("Server started on port " + PORT + " + "+Thread.currentThread().getName());
    }

    // Метод для установки консоли после создания
    // public void setConsole(SharedConsoleServer console) {
    // }

    // public InetSocketAddress getInetSocketAddress() {
    //     synchronized (addressLock) {
    //         return lastClientAddress;
    //     }
    //     //return clientAddress.get();
    // }

    // public void run(SharedConsoleServer console) {
    //     this.console = console;

    //     router = new Router(console);

    //     for (int i = 0; i < NUM_READ_THREAD_POOL; ++i) {

    //         readThreadPool.submit(() -> {
    //             SharedConsoleServer currConsole = new SharedConsoleServer(this1);

    //             //while (!Thread.currentThread().isInterrupted()) {
    //             while (true) {
    //                 try {
        
    //                     ServerRequest request = receive();

    //                     if (request != null) {
    //                         //InetSocketAddress clientAddr = request.getClientAddress();
    //                         //SharedConsoleServer.setClientAddress(clientAddr);
                            
    //                         //if (request.getRequest().getMark() != null && request.getRequest().getMark().equals(Mark.INPUT_REQUEST)) {
    //                         // if (request.getRequest().getMark() != null) {
    //                         //     try {
    //                         //         InetSocketAddress addr1 = SharedConsoleServer.getClientAddress();
    //                         //         logger.info("ADDRESS CLIENT " + (addr1 != null ? addr1.toString() : "null"));
    //                         //         // Запрос - ответ на ввод, передаем в SharedConsoleServer
    //                         //         logger.info("Start handling Client Input");
    //                         //         console.handleClientInput(request);
    //                         //         logger.info("End handling Client Input");
    //                         //     } finally {
    //                         //         //SharedConsoleServer.clearClientAddress();
    //                         //     }
                                
    //                         //     continue;
    //                         // }

    //                         //InetSocketAddress currentClientAddress = clientAddress.get();
    //                         new Thread(() -> {
    //                             logger.info("Routing request!");

                                
    //                             try {
    //                                 logger.info("Setting client addres");
    //                                 SharedConsoleServer.setClientAddress(request.getClientAddress());
    //                                 logger.info("Starting executing process");
    //                                 Response response = router.route(request.getRequest(), currConsole);
    //                                 logger.info("Responce routed and done");
                                    
    //                                 new Thread(() -> {
    //                                     logger.info("Start inet send thread process"+request.getClientAddress());
    //                                     SharedConsoleServer.setClientAddress(request.getClientAddress());
    //                                     //clientAddress.set(currentClientAddress);

    //                                     // try {
    //                                     //     send(response, clientAddress.get());
    //                                     // } finally {
    //                                     //     clientAddress.remove();
    //                                     // }
    //                                     //SharedConsoleServer.setClientAddress(clientAddr);
    //                                     logger.info("Sending responce "+response.toString());
    //                                     send(response, request.getClientAddress());
    //                                 }).start();
    //                             } finally {
    //                                 //SharedConsoleServer.clearClientAddress();
    //                             }
                                
    //                         }).start();
    //                     }

    //                     /*if (request != null) {
    //                         new Thread(() -> {
    //                             logger.info("Routing request");
    //                             Response response = router.route(request);
    //                             new Thread(() -> send(response)).start();
    //                         }).start();
    //                     }*/
        
    //                 } catch (Exception e) {
    //                     if (!Thread.currentThread().isInterrupted()) {
    //                         logger.log(Level.SEVERE, "Request processing error: ", e);
    //                     }
    //                     //System.err.println("Server error: " + e.getMessage());
    //                     //e.printStackTrace();
    //                 } finally {
    //                     //SharedConsoleServer.clearClientAddress();
    //                 }
    //             }
    //         });
    //     }
    // }
    // public synchronized ServerRequest receive(InetSocketAddress clientAddress) {
    //     ServerRequest srequest = receive();
    //     if (srequest.getClientAddress().equals(clientAddress)){
    //         return srequest;
    //     }
    //     return null;
    // }

    public synchronized ServerRequest receive() {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ServerRequest request = new ServerRequest();
        InetSocketAddress clientAddress = null; // = null
        try {
            buffer.clear();
            clientAddress = (InetSocketAddress) channel.receive(buffer);
            if (clientAddress == null) {
                return null;
            }
    
            request.setClientAdress(clientAddress);
            buffer.flip();
    
            if (buffer.remaining() == 0) {
                logger.warning("Empty packet received from " + clientAddress);
                return null;
            }
    
            logger.info("[PROCESSING] Deserializing incoming message from " + clientAddress);
            byte[] requestData = new byte[buffer.remaining()];
            buffer.get(requestData);
    
            ByteArrayInputStream byteInput = new ByteArrayInputStream(requestData);
            ObjectInputStream objectInput = new ObjectInputStream(byteInput);
            request.setRequest((Request) objectInput.readObject());
            if (request != null) {
                logger.info("[PROCESSING] Input request deserialized: " + request.toString());
                logger.info("[PROCESSING] Executing request: " + request.toString());
            }
    
            return request;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error receiving request from " + (clientAddress != null ? clientAddress : "NO CLIENT ADDRESS"), e);
            return null;
        }
    }

    // public synchronized ServerRequest receive() { // synchronized
    //     ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    //     ServerRequest request = new ServerRequest();
    //     //Request request;
    //     // logger.info(Thread.currentThread().getName() + " started recieving");
    //     // while (true) {
    //         try {
    //             buffer.clear();
    //             InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);
    //             //clientAddress.set((InetSocketAddress) channel.receive(buffer));
    //             if (clientAddress == null)
    //                 return null;
    //                 // continue;

    //             //clientAddress.set(chClientAddress);
    //             // synchronized (addressLock) {
    //             //     this.lastClientAddress = chClientAddress;
    //             // }

    //             request.setClientAdress(clientAddress);

    //             buffer.flip();

    //             if (buffer.remaining() == 0) {
    //                 logger.warning("Empty packet was received from the user " + clientAddress);
    //                 return null;
    //                 // continue;
    //             }

    //             logger.info("[PROCESSING] Deserializing incoming message");

    //             byte[] requestData = new byte[buffer.remaining()];
    //             buffer.get(requestData);

    //             ByteArrayInputStream byteInput = new ByteArrayInputStream(requestData);
    //             ObjectInputStream objectInput = new ObjectInputStream(byteInput);
    //             request.setRequest((Request) objectInput.readObject());
    //             //request = (Request) objectInput.readObject();
    //             logger.info("[PROCESSING] Input request deserialized");
    //             logger.info(request.toString());

    //             // if ("input_response".equalsIgnoreCase(request.getCommand())) {
    //             // // Обработка ответа от клиента
    //             // console.handleClientInput(request);
    //             // }

    //             logger.info(String.format("[PROCESSING] Executing request: %s", request.toString()));
    //             return request;

    //         } catch (Exception e) {
    //             logger.log(Level.WARNING, "Error details", e);
    //             return null;
    //         }
    //     // }
    // }

    public synchronized void send(Response response, InetSocketAddress clientAddress) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        try {
            buffer.clear();
            logger.info("[PROCESSING] Serializing response");
            byte[] responseData;
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
            objectOutput.writeObject(response);
            responseData = byteOutput.toByteArray();

            logger.info("[NETWORK] Sending response packet");
            ByteBuffer responseBuffer = ByteBuffer.wrap(responseData);
            channel.send(responseBuffer, clientAddress);
            logger.info(String.format("[NETWORK] Response sent to %s:%d",
                    clientAddress.getAddress().getHostAddress(),
                    clientAddress.getPort()));
        } catch (NullPointerException e) {
            logger.warning("[CLIENT] Client connection terminated unexpectedly");
            logger.log(Level.WARNING, "Client disconnect details", e);
        } catch (IOException e){
            logger.severe(String.format("[ERROR] Processing failed: %s", e.getMessage()));
            logger.log(Level.WARNING, "Error details", e);
        }
    }

    public void shutdown() {
        logger.info("[SERVER SHUTDOWN] Shutting down channel");
        //readThreadPool.shutdown();
        try {
            channel.close();
        } catch (IOException e) {
            logger.severe("[ERROR] Failed to close channel: " + e.getMessage());
        }
    }
}