package garbagegroup.cloud.tcpserver;

import java.util.List;

public interface ITCPServer {
    boolean setIoTData(int deviceId, String payload);
    String getDataById(int deviceId, String payload);
    void startServer();
    List<ServerSocketHandler> getIoTDevices();
}
