package garbagegroup.cloud.service.serviceInterface;

import garbagegroup.cloud.DTOs.*;
import garbagegroup.cloud.model.*;
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
    boolean updateBin(UpdateBinDto updatedBinDto);
    void handleIoTData(int deviceId, String data);
    boolean deleteBinById(long binId);
    List<BinDto> findAllBins();
    Optional<BinDto> findBinById(Long id);
    String getIoTData(int binId, int deviceId, String payload);
    Bin create(CreateBinDTO binDTO);
    int getAvailableDevice();
    List<NotificationBinDto> getBinsWithThresholdLessThanFillLevel();
    boolean getDeviceStatusByBinId(Long binId);
    void startPeriodicLevelRequest(int intervalSeconds);
    void stopPeriodicLevelRequest();
    void requestCurrentLevels();

    boolean sendBuzzerActivationToIoT(Long binId);
}
