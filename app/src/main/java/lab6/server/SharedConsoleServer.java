package lab6.server;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import lombok.Getter;

import lab6.shared.io.connection.Mark;
import lab6.shared.io.connection.Response;
import lab6.shared.io.console.StandartConsole;

public class SharedConsoleServer extends StandartConsole {
    private final NetworkServer network;
    private static final Logger logger = Logger.getLogger(SharedConsoleServer.class.getName());

    @Getter
    private InetSocketAddress clientAddress;

    public SharedConsoleServer(NetworkServer network) {
        this.network = network;
    }

    public void setClientAddress(InetSocketAddress address) {
        clientAddress = address;
        logger.info("[SHARED CONSOLE] Set client address: " + address);
    }

    public void offerRequest(ServerRequest request) {
        if (request == null || request.getRequest() == null) 
            return;

        if (request.getRequest().getMark() == null || 
                    (request.getRequest().getMark() != Mark.INPUT_RESPONCE && request.getRequest().getMark() != Mark.WAIT_NEXT)) {
            
            logger.warning("[SHARED CONSOLE] Invalid mark for requestQueue: " + request.getRequest().toString());
            return;
        }

        if (request.getClientAddress() == null) {
            logger.warning("[SHARED CONSOLE] Invalid clientAddress for request: " + request.getRequest().toString());
            return;
        }

        Server.getClientRequestQueue(request.getClientAddress()).offer(request); // положили нужному клиенту
    }

    @Override
    public void write(String message) {
        if (clientAddress != null) {
            logger.info("[SHARED CONSOLE] Writing message to " + clientAddress + ": " + message);
            network.send(new Response(message), clientAddress);
        } else {
            logger.warning("[SHARED CONSOLE] No client address available for sending message");
        }
    }

    @Override
    public void writeln(String message) {
        write(message + "\n");
    }

    private void catchRequestWaitNextWriteln() {
        long startTime = System.currentTimeMillis();
            long timeoutMillis = 60000;

            long timeoutMillisPoll = 300;

            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                try {
                    ServerRequest request = Server.getClientRequestQueue(clientAddress).poll(timeoutMillisPoll, TimeUnit.MILLISECONDS);

                    if (request != null && request.getRequest() != null) {
                        logger.info("[SHARED CONSOLE] Received request in writeln: " + request.getRequest() + " from " + request.getClientAddress());
                        if (request.getClientAddress().equals(clientAddress) && 
                            request.getRequest().getMark() == Mark.WAIT_NEXT) {
                            logger.info("[SHARED CONSOLE] Received WAIT_NEXT request from " + clientAddress);
                            return;
                        } else {
                            logger.warning("[SHARED CONSOLE] Unexpected request in writeln: " + request.getRequest() + " from " + request.getClientAddress());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.severe("[SHARED CONSOLE] Interrupted while waiting for WAIT_NEXT response");
                    return;
                }
            }
            logger.warning("[SHARED CONSOLE] Timeout waiting for WAIT_NEXT response from " + clientAddress);
    }

    public void writeln(String message, boolean next) {
        if (next) {
            if (clientAddress == null) {
                logger.warning("[SHARED CONSOLE] No client address set for WAIT_NEXT request");
                return;
            }
            logger.info("[SHARED CONSOLE] Sending WAIT_NEXT with message: " + message + " to " + clientAddress);
            network.send(new Response(Mark.WAIT_NEXT, message), clientAddress);

            catchRequestWaitNextWriteln();
            
        } else {
            writeln(message);
        }
    }

    public void writeln(Response response, boolean next) {
        if (clientAddress == null) {
            logger.warning("[SHARED CONSOLE] No client address set for sending response");
            return;
        }
        if (next) {
            response.setMark(Mark.WAIT_NEXT);
            logger.info("[SHARED CONSOLE] Sending WAIT_NEXT response to " + clientAddress + ": " + response+ "Thread  - "+Thread.currentThread().getName());
            network.send(response, clientAddress);

            catchRequestWaitNextWriteln();

        } else {
            //logger.info("[SHARED CONSOLE] Sending response to " + clientAddress + ": " + response);
            network.send(response, clientAddress);
        }
    }

    @Override
    public String read(String prompt) {
        if (clientAddress == null) {
            throw new IllegalStateException("[SHARED CONSOLE] No client address set for input request");
        }

        logger.info("[SHARED CONSOLE] Sending INPUT_REQUEST for prompt: " + prompt + " to " + clientAddress);
        network.send(new Response(Mark.INPUT_REQUEST, prompt), clientAddress);
        logger.info("[SHARED CONSOLE] Waiting for client response from " + clientAddress);

        long startTime = System.currentTimeMillis();
        long timeoutMillis = 60000; // Увеличили таймаут

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                ServerRequest request = Server.getClientRequestQueue(clientAddress).poll(300, TimeUnit.MILLISECONDS);
                if (request != null && request.getRequest() != null) {
                    logger.info("[SHARED CONSOLE] Received request in read: " + request.getRequest() + " from " + request.getClientAddress());
                    if (request.getClientAddress().equals(clientAddress) && 
                        request.getRequest().getMark() == Mark.INPUT_RESPONCE) {
                        String input = request.getRequest().getArgs().get(0);

                        logger.info("[SHARED CONSOLE] Received INPUT_RESPONCE from " + clientAddress + ": " + input);
                        return input;
                    } else {
                        logger.warning("[SHARED CONSOLE] Unexpected request in read: " + request.getRequest() + " from " + request.getClientAddress());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("[SHARED CONSOLE] Input interrupted", e);
            }
        }

        logger.severe("[SHARED CONSOLE] Timeout waiting for client response from " + clientAddress);
        throw new RuntimeException("[SHARED CONSOLE] Timeout waiting for client response from " + clientAddress);
    }

    public NetworkServer getNetwork() {
        return network;
    }
}
