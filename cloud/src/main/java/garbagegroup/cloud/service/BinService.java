package garbagegroup.cloud.service;

import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.repository.BinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import garbagegroup.cloud.tcpserver.TCPServer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BinService implements IBinService {
    TCPServer tcpServer;
    private BinRepository binRepository;

    @Autowired
    public BinService(BinRepository binRepository) {
        this.binRepository = binRepository;

        // When creating the BinService, we also start the TCP Server to communicate with the IoT device
        tcpServer = new TCPServer(this);
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
                }
                allHumidity = binRepository.findById(binId).get().getHumidity();

                System.out.println("Fetched new humidity hopefully");
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
    public void setTCPServer(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    public synchronized void handleIoTData(int deviceId, String data) {
        String prefix = data.substring(0, Math.min(data.length(), 5));

        if (prefix.equals("humid")) {
            String res = data.substring(6);
            double humidity = Double.parseDouble(res.substring(0, res.indexOf(":")));
            LocalDateTime dateTime = parseDateTime(res.substring(res.indexOf(":") + 1));
            saveHumidityById(deviceId, humidity, dateTime);
        }
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy:HH:mm:ss");
        return LocalDateTime.parse(dateTimeString, formatter);
    }
}
