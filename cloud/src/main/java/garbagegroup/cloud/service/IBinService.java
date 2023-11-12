package garbagegroup.cloud.service;

import garbagegroup.cloud.tcpserver.TCPServer;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface IBinService {
    public void getHumidityById(int deviceId);                      // From IoT
    public void saveHumidityById(int deviceId, double humidity);      // Save to DB
    public void setTCPServer(TCPServer tcpServer);
    Optional<Double> getHumidityById(Long binId);
}
