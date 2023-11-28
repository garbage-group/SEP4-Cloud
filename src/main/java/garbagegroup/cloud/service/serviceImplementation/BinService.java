package garbagegroup.cloud.service.serviceImplementation;

import garbagegroup.cloud.dto.UpdateBinDto;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.model.Level;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.service.serviceInterface.IBinService;
import garbagegroup.cloud.tcpserver.ITCPServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BinService implements IBinService {
    ITCPServer tcpServer;
    private IBinRepository binRepository;

    @Autowired
    public BinService(IBinRepository binRepository, ITCPServer tcpServer) {
        this.binRepository = binRepository;

        // When creating the BinService, we also start the TCP Server to communicate with the IoT device
        tcpServer.startServer();
        this.setTCPServer(tcpServer);
    }


    @Override
    public synchronized Optional<Humidity> getCurrentHumidityByBinId(Long binId) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isPresent()) {
            List<Humidity> allHumidity = binOptional.get().getHumidity();

            // Sort the list of humidity values by date and time in descending order
            allHumidity.sort(Comparator.comparing(Humidity::getDateTime).reversed());

            if (allHumidity.get(0) != null) {
                LocalDateTime measurementDateTime = allHumidity.get(0).getDateTime();
                LocalDateTime currentDateTime = LocalDateTime.now();

                // Check if the measurement is recent (within the last hour)
                if (Duration.between(measurementDateTime, currentDateTime).getSeconds() > 3600) {
                    String responseFromIoT = tcpServer.getHumidityById(binId.intValue());
                    if (responseFromIoT.contains("Device with ID " + binOptional.get().getDeviceId() + " is currently unavailable"))
                        return Optional.empty();
                    handleIoTData(binId.intValue(), responseFromIoT);
                }

                allHumidity = binRepository.findById(binId).get().getHumidity();
                allHumidity.sort(Comparator.comparing(Humidity::getDateTime).reversed());
                return Optional.of(allHumidity.get(0));
            } else {
                // Handle the case where the Bin with the given ID is not found
                return Optional.empty();
            }
        }
        return Optional.empty();
    }


    @Override
    public synchronized void saveHumidityById(int binId, double humidity, LocalDateTime dateTime) {
        System.out.println("About to save humidity: " + humidity + " with date and time: " + dateTime + " to bin with ID: " + binId);

        Optional<Bin> optionalBin = binRepository.findById((long) binId);
        if (optionalBin.isPresent()) {
            Bin bin = optionalBin.get();
            Humidity newHumidity = new Humidity(bin, humidity, dateTime);
            bin.getHumidity().add(newHumidity);
            binRepository.save(bin);
        }
    }

    @Override
    public void setTCPServer(ITCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    public synchronized void handleIoTData(int deviceId, String data) {
        System.out.println("The received stuff is: " + data);
        String prefix = data.substring(0, Math.min(data.length(), 5));

        if (prefix.equals("humid")) {
            String res = data.substring(6);
            double humidity = Double.parseDouble(res);
            LocalDateTime dateTime = LocalDateTime.now();
            saveHumidityById(deviceId, humidity, dateTime);
        }

    }


    public void updateBin(UpdateBinDto updatedBinDto) {
        Optional<Bin> binOptional = binRepository.findById(updatedBinDto.getId());
        binOptional.ifPresent(bin -> {
            try {
                updateBinFields(bin, updatedBinDto);
                binRepository.save(bin);
                communicateWithIoT(bin.getId(), bin.getFillThreshold());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Error updating bin: " + e.getMessage());
            }
        });
    }


    private void updateBinFields(Bin bin, UpdateBinDto updatedBinDto) {
        if (isValidLongitude(updatedBinDto.getLongitude())) {
            bin.setLongitude(updatedBinDto.getLongitude());
        } else {
            throw new IllegalArgumentException("Invalid longitude value");
        }

        if (isValidLatitude(updatedBinDto.getLatitude())) {
            bin.setLatitude(updatedBinDto.getLatitude());
        } else {
            throw new IllegalArgumentException("Invalid latitude value");
        }

        if (isValidThreshold(updatedBinDto.getFillthreshold())) {
            double lastLevelReading = getLastLevelReading(bin.getId());
            double newFillThreshold = updatedBinDto.getFillthreshold();

            if (newFillThreshold < lastLevelReading) {
                throw new IllegalArgumentException("FillThreshold cannot be set lower than the last level reading");
            }

            bin.setFillThreshold(newFillThreshold);
        } else {
            throw new IllegalArgumentException("Invalid fill threshold value");
        }
    }

    private void communicateWithIoT(Long binId,double fillThreshold) {
        try {
            String response = tcpServer.setFillThreshold(binId,fillThreshold);
            System.out.println("Response from IoT device: " + response+" : "+fillThreshold);
        } catch (Exception e) {
            System.out.println("Error communicating with IoT device: " + e.getMessage());
        }
    }


    private Bin convertToBin(UpdateBinDto updatedBinDto) {
        Bin bin = new Bin();
        bin.setId(updatedBinDto.getId());
        bin.setLongitude(updatedBinDto.getLongitude());
        bin.setLatitude(updatedBinDto.getLatitude());
        bin.setFillThreshold(updatedBinDto.getFillthreshold());
        return bin;
    }

    private boolean isValidLongitude(Double longitude) {
        return longitude >= -180 && longitude <= 180;
    }

    private boolean isValidLatitude(Double latitude) {
        return latitude >= -90 && latitude <= 90;
    }

    private boolean isValidThreshold(Double threshold) {
        return threshold >= 0 && threshold <= 100;
    }

    private double getLastLevelReading(Long binId) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isPresent()) {
            List<Level> alllevel = binOptional.get().getLevel();

            alllevel.sort(Comparator.comparing(Level::getDateTime).reversed());
            if (!alllevel.isEmpty()) {
                return alllevel.get(0).getValue();
            }
            return 0;
        }
        return 0;
    }

}
