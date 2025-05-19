package lab6.client.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import lab6.shared.io.connection.Request;
import lab6.shared.io.connection.Response;
import lab6.shared.io.console.StandartConsole;

public class SharedClient extends StandartConsole {
    private static final String SERVER_IP = "192.168.10.80";
    private static final int SERVER_PORT = 2223;
    private static final int TIMEOUT = 15000; // было 6000
    private static final int MAX_ATTEMPTS = 3;

    private DatagramSocket socket;

    public SharedClient() throws SocketException {
        socket = new DatagramSocket();
        
    }

    public Response sendRecive(Request request) {
        try {
            socket.setSoTimeout(TIMEOUT);

            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
            objectOutput.writeObject(request);
            byte[] requestData = byteOutput.toByteArray();

            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            DatagramPacket packet = new DatagramPacket(requestData, requestData.length, serverAddress, SERVER_PORT);

            boolean responseReceived = false;
            int attempts = 0;
            Response response = null;

            while (!responseReceived && attempts < MAX_ATTEMPTS) {
                try {
                    socket.send(packet);
                    // writeln("    # Request sended, waiting responce");

                    byte[] buffer = new byte[65535];
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responsePacket);
                    // writeln("    # Responce recieved");

                    //writeln("DEBUG: Response packet received from " + responsePacket.getAddress() + ":" + responsePacket.getPort());

                    ByteArrayInputStream byteInput = new ByteArrayInputStream(responsePacket.getData(), 0,
                            responsePacket.getLength());
                    ObjectInputStream objectInput = new ObjectInputStream(byteInput);
                    response = (Response) objectInput.readObject();

                    responseReceived = true;
                    // writeln("    # Response received successfully.");

                } catch (SocketTimeoutException e) {
                    ++attempts;
                    writeln("   # Attempt " + (attempts) + " of " + MAX_ATTEMPTS + " to reach server...");
                    writeln("   # Server did not respond within " + (TIMEOUT / 1000) + " seconds.");
                    if (attempts >= MAX_ATTEMPTS) {
                        writeln("   # All attempts failed. Server is unavailable.");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    writeln("   # Error sending request: " + e.getMessage());
                    break;
                }
            }
            // writeln("    # returning responce:");
            return response;

        } catch (IOException e) {
            writeln("   # Network error: " + e.toString());
            socket.close();
            return new Response("Network error");
        }
    }
}