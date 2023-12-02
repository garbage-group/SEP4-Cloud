package garbagegroup.cloud.service.serviceInterface;

import garbagegroup.cloud.DTOs.NotificationBinDto;
import garbagegroup.cloud.DTOs.UpdateBinDto;
import garbagegroup.cloud.DTOs.BinDto;
import garbagegroup.cloud.DTOs.CreateBinDTO;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.model.Level;
import garbagegroup.cloud.model.Temperature;
import garbagegroup.cloud.tcpserver.ITCPServer;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public interface IBinService {

    Optional<Humidity> getCurrentHumidityByBinId(Long binId);
    boolean saveHumidityByBinId(int binId, double humidity, LocalDateTime dateTime);      // Save to DB
    Optional<Temperature> getCurrentTemperatureByBinId(Long binId);
    Optional<Level> getCurrentFillLevelByBinId(Long binId);
    boolean saveFillLevelByBinId(int binId, double fillLevel, LocalDateTime dateTime);     // Save to DB
    boolean saveTemperatureByBinId(int binId, double fillLevel, LocalDateTime dateTime);     // Save to DB
    void setTCPServer(ITCPServer tcpServer);

    void updateBin(UpdateBinDto updatedBinDto);
    void handleIoTData(int deviceId, String data);
    void deleteBinById(long binId);
    List<BinDto> findAllBins();
    Optional<BinDto> findBinById(Long id);
    void getIoTData(int binId, int deviceId, String payload);
    Bin create(CreateBinDTO binDTO);
    int getAvailableDevice();

    List<NotificationBinDto> getBinsWithThresholdLessThanFillLevel();
}
