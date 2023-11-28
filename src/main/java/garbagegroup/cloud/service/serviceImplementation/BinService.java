package garbagegroup.cloud.service.serviceImplementation;

import garbagegroup.cloud.dto.BinDto;
import garbagegroup.cloud.dto.DTOConverter;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
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
    private DTOConverter dtoconverter;

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

    @Override
    public void deleteBinById(long binId) {
        if (binRepository.existsById(binId)) {
            binRepository.deleteById(binId);
        } else {
            throw new NoSuchElementException("Bin with id " + binId + " not found");
        }
    }

    @Override
    public List<BinDto> findAllBins() {
        List<Bin> bins = binRepository.findAll();
        List<BinDto> binDtos = new ArrayList<>();
        for (Bin bin: bins) {
            BinDto dto = dtoconverter.convertToBinDto(bin);
            binDtos.add(dto);
        }
        return binDtos;
    }

    @Override
    public Optional<BinDto> findBinById(Long id) {
        Optional<Bin> binOptional = binRepository.findById(id);
        if (binOptional.isPresent()) {
            Bin bin = binOptional.get();
            BinDto binDto = dtoconverter.convertToBinDto(bin);
            return Optional.of(binDto);
        } else {
            return Optional.empty();
        }
    }

}
