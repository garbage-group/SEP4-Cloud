package garbagegroup.cloud.tcpserver;

import java.util.List;

public interface ITCPServer {
    public String getHumidityById(int deviceId);
    void startServer();
    public List<ServerSocketHandler> getIoTDevices();
}
