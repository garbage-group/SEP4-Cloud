package garbagegroup.cloud.service;

import garbagegroup.cloud.DTOs.CreateBinDTO;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.tcpserver.ITCPServer;
import garbagegroup.cloud.tcpserver.TCPServer;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public interface IBinService {

    Optional<Humidity> getCurrentHumidityByBinId(Long binId);
    public void saveHumidityById(int deviceId, double humidity, LocalDateTime dateTime);      // Save to DB
    public void setTCPServer(ITCPServer tcpServer);
    public void handleIoTData(int deviceId, String data);
    public Bin create(CreateBinDTO binDTO);

    public int getAvailableDevice();
}
