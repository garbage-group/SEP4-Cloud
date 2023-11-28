package garbagegroup.cloud.tcpserver;

import org.hibernate.sql.exec.ExecutionException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Component
public class TCPServer implements ITCPServer, Runnable {
    ServerSocket serverSocket;
    ServerSocketHandler socketHandler;
    List<ServerSocketHandler> IoTDevices = new ArrayList<>();

    public TCPServer() {
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
    public String getDataById(int deviceId, String payload) {
        String response = "";
        for (ServerSocketHandler ssh: IoTDevices) {
            if (ssh.getDeviceId() == deviceId) {
                response = ssh.sendMessage(payload);
            }
        }
        return response;
    }

    /**
     * Sets the fill threshold for a bin with the provided ID if associated with an IoT device.
     * Sends the threshold to the associated IoT device for updating.
     *
     * @param binId       The ID of the bin for which the fill threshold is to be set.
     * @param newThreshold The new fill threshold value to be set.
     * @return A message indicating the status of setting the fill threshold. If the bin is not associated with any IoT device, a corresponding message is returned.
     */
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

    /**
     * @return All currently connected IoT devices
     */
    @Override
    public List<ServerSocketHandler> getIoTDevices() {
        return IoTDevices;
    }

    /**
     * Requests a serial number from the IoT device
     * @return device's serial number
     */
    public int getIoTSerialNumber() {
        String response = socketHandler.sendMessage("getSerialNumber");     // This will return the serial number of the IoT device (if ok), which we need to find out which bin it is attached to
        return Integer.parseInt(response);
    }
}