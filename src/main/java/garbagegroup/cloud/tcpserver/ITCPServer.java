package garbagegroup.cloud.tcpserver;

import java.util.List;

public interface ITCPServer {

    //public String setFillThreshold(Long binId, double newThreshold);
    public String setDataById(int deviceId, String payload);
    public String getDataById(int deviceId, String payload);
    void startServer();
    public List<ServerSocketHandler> getIoTDevices();
}
