package garbagegroup.cloud.tcpserver;

import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.service.BinService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TCPServer implements ITCPServer, Runnable {
    private static int nextId = 1;
    private ServerSocket serverSocket;
    private BinService binService;
    private ServerSocketHandler socketHandler;
    private List<ServerSocketHandler> IoTDevices;

    public TCPServer(BinService binService) {
        this.binService = binService;
        IoTDevices = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(2910);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Server started. Waiting for connections...");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                // Create a new thread to handle the client connection
                socketHandler = new ServerSocketHandler(generateId(), clientSocket, this, binService);
                IoTDevices.add(socketHandler);
                new Thread(socketHandler).start();

                System.out.println("Client connected. Giving it ID: " + socketHandler.getDeviceId());
            } catch (IOException e) {
                System.out.println("Client with ID " + socketHandler.getDeviceId() + " disconnected");
                //e.printStackTrace();
            }
        }
    }

    public void startServer() {
        new Thread(this).start();
    }

    public static int generateId() { return nextId++; }

    @Override
    public String getHumidityById(int deviceId) {
        String response = "";
        if (IoTDevices.size() == 0) response = "Device with ID " + deviceId + " is currently unavailable";
        for (ServerSocketHandler ssh: IoTDevices) {
            if (ssh.getDeviceId() == deviceId) {
                ssh.sendMessage("getHumidity");
                response = "OK";
            }
            else {
                response = "Device with ID " + deviceId + " is currently unavailable";
            }
        }
        return response;
    }
}
