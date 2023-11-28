package garbagegroup.cloud.tcpserver;

import java.util.List;

public interface ITCPServer {
    public String getDataById(int deviceId, String payload);
    void startServer();
    public List<ServerSocketHandler> getIoTDevices();
}
