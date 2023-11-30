package garbagegroup.cloud.tcpserver;

import java.util.List;

public interface ITCPServer {

    public String setFillThreshold(double newThreshold);
    public String getDataById(int deviceId, String payload);
    void startServer();
    public List<ServerSocketHandler> getIoTDevices();
}
