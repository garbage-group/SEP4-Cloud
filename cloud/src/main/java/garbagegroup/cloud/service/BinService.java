package garbagegroup.cloud.service;


import garbagegroup.cloud.repository.BinRepository;
import garbagegroup.cloud.tcpserver.TCPServer;
import org.springframework.stereotype.Service;

@Service
public class BinService implements IBinService {
    TCPServer tcpServer;
    BinRepository binRepository;

    public BinService(BinRepository binRepository) {
        this.binRepository = binRepository;

        // When creating the BinService, we also start the TCP Server to communicate with the IoT device
        TCPServer tcpServer = new TCPServer(this, 1920);
        tcpServer.startServer();
        this.setTCPServer(tcpServer);
    }

    @Override
    public void getHumidityById(int deviceId) {
        tcpServer.getHumidityById(deviceId);
    }

    @Override
    public void saveHumidityById(int deviceId, double humidity) {
        // This is where we tell the repository to save the humidity
    }

    @Override
    public void setTCPServer(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }
}
