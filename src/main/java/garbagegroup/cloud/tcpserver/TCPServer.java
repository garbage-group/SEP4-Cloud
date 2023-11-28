package garbagegroup.cloud.tcpserver;

import org.hibernate.sql.exec.ExecutionException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class TCPServer implements ITCPServer, Runnable {
    private static int nextId = 1;
    private ServerSocket serverSocket;
    private ServerSocketHandler socketHandler;
    private List<ServerSocketHandler> IoTDevices;

    public TCPServer() {
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
                socketHandler = new ServerSocketHandler(generateId(), clientSocket);
                IoTDevices.add(socketHandler);
                System.out.println("Client connected. Giving it ID: " + socketHandler.getDeviceId());
            } catch (IOException e) {
                System.out.println("Client with ID " + socketHandler.getDeviceId() + " disconnected");
                //e.printStackTrace();
            }
        }
    }

    @Override
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
                response = ssh.sendMessage("getHumidity");
            }
            else {
                response = "Device with ID " + deviceId + " is currently unavailable";
                System.out.println(response);
            }
        }
        return response;
    }

    @Override
    public String setFillThreshold(Long binId, double newThreshold) {
        String message = "setFillThreshold(" + newThreshold + ")";
        String response = "Bin with ID " + binId + " is not associated with any IoT device";
        boolean isAssociated = false;

        for (ServerSocketHandler ssh : IoTDevices) {
            if (ssh.getDeviceId() == binId) {
                response = ssh.sendMessage("setFillThreshold");
                isAssociated = true; // Set flag to indicate association found
                break;
            }
        }
        if (!isAssociated) {
            System.out.println(response);
        }

        return response;
    }








}