package garbagegroup.cloud.service;

import garbagegroup.cloud.DTOs.CreateBinDTO;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.tcpserver.ITCPServer;
import garbagegroup.cloud.tcpserver.ServerSocketHandler;
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

            if (!allHumidity.isEmpty()) {
                LocalDateTime measurementDateTime = allHumidity.get(0).getDateTime();
                LocalDateTime currentDateTime = LocalDateTime.now();

                // Check if the measurement is recent (within the last hour)
                if (Duration.between(measurementDateTime, currentDateTime).getSeconds() > 3600) {
                    String responseFromIoT = tcpServer.getHumidityById(binId.intValue());
                    if (responseFromIoT.contains("unavailable"))
                        return Optional.empty();
                    handleIoTData(binId.intValue(), responseFromIoT);
                }

                allHumidity = binOptional.get().getHumidity();
                allHumidity.sort(Comparator.comparing(Humidity::getDateTime).reversed());
                return Optional.of(allHumidity.get(0));
            } else {
                //handle that the list of humidities is empty
                return Optional.empty();
            }
        }
        // Handle the case where the Bin with the given ID is not found
        return Optional.empty();
    }


    @Override
    public synchronized boolean saveHumidityById(int binId, double humidity, LocalDateTime dateTime) {
        System.out.println("About to save humidity: " + humidity + " with date and time: " + dateTime + " to bin with ID: " + binId);

        try {
            Optional<Bin> optionalBin = binRepository.findById((long) binId);
            Bin bin = optionalBin.get();
            Humidity newHumidity = new Humidity(bin, humidity, dateTime);
            if (bin.getHumidity() == null) {
                List<Humidity> humidityList = new ArrayList<>();
                humidityList.add(newHumidity);
                bin.setHumidity(humidityList);
            }
            else bin.getHumidity().add(newHumidity);

            try {
                binRepository.save(bin);
                return true;
            } catch (Exception e) {
                System.err.println("Error saving humidity with Bin Id: " + binId + ".\n" + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error finding bin with Id: " + binId + ".\n" + e.getMessage());
            return false;
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

    /**
     * This function creates a bin with an IoT device in the following way:
     * Online device - if there is an online device that is not connected to a bin, this device is assigned to the bin
     * Fake device - if there is no online device available, a fake device is created and assigned to the bin, as well as fake data in the DB
     * @param binDTO
     * @return create bin
     */
    @Override
    public Bin create(CreateBinDTO binDTO) {
        Bin createdBin = null;
        Bin newBin = new Bin(binDTO.getLocation(), binDTO.getCapacity(), binDTO.getFillThreshold(), null, null);
        int deviceId = getAvailableDevice();
        if (deviceId == 0) {
            Random random = new Random();
            // There is no IoT Device online so just make up some IoT device
            int randomDeviceId = random.nextInt(2000 - 1001) + 1000;
            newBin.setDeviceId(randomDeviceId);
            createdBin = binRepository.save(newBin);
            loadFakeIoTDeviceData(newBin.getId().intValue());
        }
        else {
            newBin.setDeviceId(deviceId);
            createdBin = binRepository.save(newBin);
        }
        return createdBin;
    }

    /**
     * This function returns an online IoT device that does not belong to any bin
     * @return deviceId of the available device
     */
    @Override
    public int getAvailableDevice() {
        List<ServerSocketHandler> IoTDevices = tcpServer.getIoTDevices();   // Get all online devices
        if (IoTDevices == null) return 0;
        if (IoTDevices.size() == 0) return 0;
        else {
            List<Bin> bins = binRepository.findAll();   // Fetch all bins
            for (Bin bin : bins) {      // Check if there is an online device that has not been assigned to a bin
                // Remove a device from list IoTDevices if the device belongs to a bin already
                IoTDevices.removeIf(device -> bin.getDeviceId() == device.getDeviceId());
            }
            if (IoTDevices.size() >= 1) { // If there is a device left, return the device ID of the first device
                return IoTDevices.get(0).getDeviceId();
            }
            else return 0;  // Else return 0
        }
    }

    /**
     * This function saves fakes IoT data for a non-existent IoT device
     * @param binId
     */
    public void loadFakeIoTDeviceData(int binId) {
        // Load some fake data
        saveHumidityById(binId, 26.0, LocalDateTime.now());
        saveHumidityById(binId, 32.0, LocalDateTime.now());
        saveHumidityById(binId, 33.0, LocalDateTime.now());
    }
}
