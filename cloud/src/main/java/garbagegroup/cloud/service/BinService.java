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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public Optional<Double> getCurrentHumidityByBinId(Long binId) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isPresent()) {
            List<Humidity> allHumidity = binOptional.get().getHumidity();

            if (allHumidity.get(0) != null) {
                LocalDateTime measurementDateTime = allHumidity.get(0).getDateTime();
                LocalDateTime currentDateTime = LocalDateTime.now();

                // Check if the measurement is recent (within the last hour)
                if (Duration.between(measurementDateTime, currentDateTime).getSeconds() > 3600) {
                    String responseFromIoT = tcpServer.getHumidityById(binId.intValue());
                    handleIoTData(binId.intValue(), responseFromIoT);
                }

                return Optional.of(allHumidity.get(0).getValue());
            } else {
                // Handle the case where the Bin with the given ID is not found
                return Optional.empty();
            }
        }
        return Optional.empty();
    }


    @Override
    public void saveHumidityById(int deviceId, double humidity, LocalDateTime dateTime) {
        System.out.println("About to save humidity: " + humidity + " with date and time: " + dateTime + " to device with ID: " + deviceId);
        // This is where we tell the repository to save the humidity
        //binRepository.save(new Humidity(Long.valueOf(deviceId), humidity, dateTime));
    }

    @Override
    public void setTCPServer(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @Override
    public void handleIoTData(int deviceId, String data) {
        String prefix = data.substring(0, Math.min(data.length(), 5));

        if (prefix.equals("humid")) {
            double humidity = Double.parseDouble(data.substring(6));
            String dateTimeString = extractDateTimeString(data);
            LocalDateTime dateTime = parseDateTime(dateTimeString);
            saveHumidityById(deviceId, humidity, dateTime);
        }
    }

    private String extractDateTimeString(String data) {
        int start = data.indexOf("datetime:") + "datetime:".length();
        int end = data.indexOf(",", start);
        return data.substring(start, end);
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd,HH:mm:ss");
        return LocalDateTime.parse(dateTimeString, formatter);
    }
}
