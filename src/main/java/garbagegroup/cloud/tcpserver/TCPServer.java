package garbagegroup.cloud.tcpserver;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Component
public class TCPServer implements ITCPServer, Runnable {
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
                socketHandler = new ServerSocketHandler(clientSocket);
                IoTDevices.add(socketHandler);
                int serialNumber = getIoTSerialNumber();
                socketHandler.setDeviceId(serialNumber);    // Setting the actual serial number
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

    /**
     * Method that requests humidity data from the IoT device and returns it to the BinService
     * @param deviceId
     * @return  humidity data from the IoT device, if available, otherwise String that indicates that the device is not available
     */
    @Override
    public String getHumidityById(int deviceId) {
        String response = "";
        if (IoTDevices.isEmpty()) response = "Device with ID " + deviceId + " is currently unavailable";
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

    /**
     * @return All currently connected IoT devices
     */
    public List<ServerSocketHandler> getIoTDevices() {
        return IoTDevices;
    }

    /**
     * Requests a serial number from the IoT device
     * @return device's serial number
     */
    public int getIoTSerialNumber() {
        String response = socketHandler.sendMessage("getStatus");     // This will return the serial number of the IoT device (if ok), which we need to find out which bin it is attached to
        return Integer.parseInt(response);
    }
}