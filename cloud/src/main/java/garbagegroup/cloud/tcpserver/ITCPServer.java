package garbagegroup.cloud.tcpserver;

public interface ITCPServer {
    public void getHumidityById(int deviceId);
    public void handleIoTData(int deviceId, String data);
}
