package garbagegroup.cloud.service;

import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.repository.BinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import garbagegroup.cloud.tcpserver.TCPServer;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BinService implements IBinService{
    TCPServer tcpServer;
    private BinRepository binRepository;

    @Autowired
    public BinService(BinRepository binRepository) {
        this.binRepository = binRepository;

        // When creating the BinService, we also start the TCP Server to communicate with the IoT device
        TCPServer tcpServer = new TCPServer(this, 1920);
        tcpServer.startServer();
        this.setTCPServer(tcpServer);
    }


    //look up a 'Bin' by its id, retrieve the associate 'Measurement' and return the humidity value
    @Override
    public Optional<Double> getHumidityById(Long binId) {
        Optional<Bin> bin = binRepository.findById(binId);

        return bin.map(value -> value.getMeasurement().getHumidity());
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
