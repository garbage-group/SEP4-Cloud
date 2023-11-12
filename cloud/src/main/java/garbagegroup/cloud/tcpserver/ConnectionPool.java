package garbagegroup.cloud.tcpserver;

import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {
    private List<ServerSocketHandler> IoTDevices = new ArrayList<>();

    public void addDevice(ServerSocketHandler ssh) {IoTDevices.add(ssh);}

    public void removeDevice(ServerSocketHandler ssh) {
        if (IoTDevices.contains(ssh)) {
            IoTDevices.remove(ssh);
        }
    }

    public void sendMessage(int deviceId, String message) {
        System.out.println("Sending " + message + " to device with ID: " + deviceId);
        for (ServerSocketHandler ssh: IoTDevices) {
            if (ssh.getDeviceId() == deviceId) {
                ssh.sendMessage(message);
            }
        }
    }
}
