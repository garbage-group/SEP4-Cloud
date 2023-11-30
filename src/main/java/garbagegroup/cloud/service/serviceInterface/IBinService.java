package garbagegroup.cloud.service.serviceInterface;

import garbagegroup.cloud.DTOs.BinDto;
import garbagegroup.cloud.DTOs.CreateBinDTO;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.tcpserver.ITCPServer;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public interface IBinService {

    Optional<Humidity> getCurrentHumidityByBinId(Long binId);
    public boolean saveHumidityById(int binId, double humidity, LocalDateTime dateTime);      // Save to DB
    public void saveFillLevelById(int binId, double fillLevel, LocalDateTime dateTime);     // Save to DB
    public void setTCPServer(ITCPServer tcpServer);
    public void handleIoTData(int deviceId, String data);
    public void deleteBinById(long binId);
    public List<BinDto> findAllBins();
    public Optional<BinDto> findBinById(Long id);
    public void getIoTData(int binId, int deviceId, String payload);
    public Bin create(CreateBinDTO binDTO);
    public int getAvailableDevice();
}
