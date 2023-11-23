package garbagegroup.cloud.tcpserver;

import org.springframework.stereotype.Component;

@Component
public interface ITCPServer {
    public String getHumidityById(int deviceId);
    void startServer();
}
