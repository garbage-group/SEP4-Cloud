package garbagegroup.cloud.tcpserver;
public interface ITCPServer {
    public String getHumidityById(int deviceId);
    public String setFillThreshold(Long binId, double newThreshold);
    void startServer();
}
