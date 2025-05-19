package lab6.server;

import java.net.InetSocketAddress;

import lab6.shared.io.connection.Request;

public class ServerRequest {
    private InetSocketAddress clientAddress;
    private Request request;

    public ServerRequest() {}

    public ServerRequest(InetSocketAddress clientAddress, Request request) {
        this.clientAddress = clientAddress;
        this.request = request;
    }

    public InetSocketAddress getClientAddress() {
        return clientAddress;
    }

    public void setClientAdress(InetSocketAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    @Override
    public String toString() {
        return request.toString() + " " + clientAddress.toString();
    } 
}