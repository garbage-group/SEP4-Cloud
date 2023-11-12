package garbagegroup.cloud.service;

import garbagegroup.cloud.tcpserver.ServerSocketHandler;
import garbagegroup.cloud.tcpserver.TCPServer;
import org.springframework.stereotype.Service;

@Service
public interface IBinService {
    public void getHumidityById(int deviceId);                      // From IoT
    public void saveHumidityById(int deviceId, double humidity);      // Save to DB
    public void setTCPServer(TCPServer tcpServer);
}
