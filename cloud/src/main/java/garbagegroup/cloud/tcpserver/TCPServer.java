package garbagegroup.cloud.tcpserver;

import garbagegroup.cloud.service.BinService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements ITCPServer {
    private static int nextId = 1;
    private ServerSocket serverSocket;
    private BinService binService;
    private ServerSocketHandler socketHandler;
    private ConnectionPool connectionPool;

    public TCPServer(BinService binService, int port) {
        this.binService = binService;
        connectionPool = new ConnectionPool();

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        System.out.println("Server started. Waiting for connections...");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Create a new thread to handle the client connection
                socketHandler = new ServerSocketHandler(generateId(), clientSocket, this);
                connectionPool.addDevice(socketHandler);
                new Thread(socketHandler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int generateId() { return nextId++; }

    @Override
    public void getHumidityById(int deviceId) {
        connectionPool.sendMessage(deviceId,"getHumidity");
        //socketHandler.sendMessage("getHumidity");
    }

    @Override
    public void handleIoTData(int deviceId, String data) {
        String prefix = data.substring(0, Math.min(data.length(), 5));

        if (prefix.equals("humid")) {
            double humidity = Double.parseDouble(data.substring(6));
            binService.saveHumidityById(deviceId, humidity);
        }
    }
}
