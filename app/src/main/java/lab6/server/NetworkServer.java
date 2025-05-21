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

import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;

public class NetworkServer {
    private static final int PORT = 2223;
    private static final int BUFFER_SIZE = 65535;
    private DatagramChannel channel;

    private static final Logger logger = Logger.getLogger(NetworkServer.class.getName());


    public NetworkServer() throws IOException {
        channel = DatagramChannel.open();
        channel.socket().setReuseAddress(true);
        channel.configureBlocking(false);
        try {
            channel.bind(new InetSocketAddress(PORT));
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        logger.info("Server started on port " + PORT + " + "+Thread.currentThread().getName());
    }

    public synchronized ServerRequest receive() {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ServerRequest request = new ServerRequest();
        InetSocketAddress clientAddress = null;
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
        try {
            channel.close();
        } catch (IOException e) {
            logger.severe("[ERROR] Failed to close channel: " + e.getMessage());
        }
    }
}