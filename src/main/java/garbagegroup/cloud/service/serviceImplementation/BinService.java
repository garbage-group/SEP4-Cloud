package garbagegroup.cloud.service.serviceImplementation;

import garbagegroup.cloud.DTOs.CreateBinDTO;
import garbagegroup.cloud.DTOs.UpdateBinDto;
import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.model.Level;
import garbagegroup.cloud.model.User;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.service.serviceInterface.IBinService;
import garbagegroup.cloud.tcpserver.ITCPServer;
import garbagegroup.cloud.tcpserver.ServerSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    public BinService() {
    }

    /**
     * Fetches Humidity from the IoT device if the reading in DB is older than 1 hour, otherwise it gets from the IoT device
     *
     * @param binId
     * @return Optional<Humidity>
     */
    @Override
    public Optional<Humidity> getCurrentHumidityByBinId(Long binId) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isEmpty()) {
            return Optional.empty();
        }

        Bin bin = binOptional.get();
        List<Humidity> allHumidity = bin.getHumidity();
        allHumidity.sort(Comparator.comparing(Humidity::getDateTime).reversed());   // To organize the humidity readings from newest to oldest

        LocalDateTime measurementDateTime = (allHumidity.isEmpty()) ? null : allHumidity.get(0).getDateTime();  // Get the measurement time if the humidity readings are not null
        if (measurementDateTime == null || isMeasurementOld(measurementDateTime)) {
            getIoTData(bin.getId().intValue(), bin.getDeviceId(), "getHumidity");
            bin = binRepository.findById(binId).orElse(bin);
            allHumidity = bin.getHumidity();
            allHumidity.sort(Comparator.comparing(Humidity::getDateTime).reversed());
        }

        return (allHumidity.isEmpty()) ? Optional.empty() : Optional.of(allHumidity.get(0));
    }

    /**
     * Checks whether the oldest humidity measurement from DB is older than 1 hour
     *
     * @param measurementDateTime
     * @return boolean
     */
    private boolean isMeasurementOld(LocalDateTime measurementDateTime) {
        return Duration.between(measurementDateTime, LocalDateTime.now()).getSeconds() > Duration.ofHours(1).getSeconds();
    }


    /**
     * If the IoT device is active, it fetches it from it, if not, it fakes it
     * @param binId
     * @return
     */
    @Override
    public void getIoTData(int binId, int deviceId, String payload) {
        if (hasActiveDevice(deviceId)) {    // Fetch real data from IoT device
            String responseFromIoT = tcpServer.getDataById(deviceId, payload);
            handleIoTData(binId, responseFromIoT);
        }
        else loadFakeIoTDeviceData(binId, payload);     // Or load some fake data
    }



    /**
     * Saves humidity data fetched from the IoT to the DB
     *
     * @param binId
     * @param humidity
     * @param dateTime
     */
    @Override
    public void saveHumidityById(int binId, double humidity, LocalDateTime dateTime) {
        System.out.println("About to save humidity: " + humidity + " with date and time: " + dateTime + " to bin with ID: " + binId);

        Optional<Bin> optionalBin = binRepository.findById((long) binId);
        if (optionalBin.isPresent()) {
            Bin bin = optionalBin.get();
            Humidity newHumidity = new Humidity(bin, humidity, dateTime);
            if (bin.getHumidity() == null) {
                List<Humidity> humidityList = new ArrayList<>();
                humidityList.add(newHumidity);
                bin.setHumidity(humidityList);
            } else bin.getHumidity().add(newHumidity);
            binRepository.save(bin);
        }
    }

    /**
     * Save fill Level data fetched from the IoT device to the DB
     *
     * @param binId
     * @param fillLevel
     * @param dateTime
     */
    @Override
    public void saveFillLevelById(int binId, double fillLevel, LocalDateTime dateTime) {
        System.out.println("About to save fill level: " + fillLevel + " with date and time: " + dateTime + " to bin with ID: " + binId);

        Optional<Bin> optionalBin = binRepository.findById((long) binId);
        if (optionalBin.isPresent()) {
            Bin bin = optionalBin.get();
            Level newFillLevel = new Level(bin, fillLevel, dateTime);
            if (bin.getFillLevels() == null) {
                List<Level> fillLevelList = new ArrayList<>();
                fillLevelList.add(newFillLevel);
                bin.setFillLevels(fillLevelList);
            } else bin.getFillLevels().add(newFillLevel);
            binRepository.save(bin);
        }
    }

    /**
     * Sets the tcpServer
     *
     * @param tcpServer
     */
    @Override
    public void setTCPServer(ITCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    /**
     * Based on which data has been received from the IoT device, it saves it to the correct table in DB
     *
     * @param binId
     * @param data
     */
    public void handleIoTData(int binId, String data) {
        System.out.println("The received stuff is: " + data);
        String prefix = data.substring(0, Math.min(data.length(), 5));
        String res = data.substring(6);

        if (prefix.equals("humid")) {
            double humidity = Double.parseDouble(res);
            LocalDateTime dateTime = LocalDateTime.now();
            saveHumidityById(binId, humidity, dateTime);
        }
        if (prefix.equals("level")) {
            double humidity = Double.parseDouble(res);
            LocalDateTime dateTime = LocalDateTime.now();
            saveFillLevelById(binId, humidity, dateTime);
        }

    }

    /**
     * This function creates a bin with an IoT device in the following way:
     * Online device - if there is an online device that is not connected to a bin, this device is assigned to the bin
     * Fake device - if there is no online device available, a fake device is created and assigned to the bin, as well as fake data in the DB
     *
     * @param binDTO
     * @return create bin
     */
    @Override
    public Bin create(CreateBinDTO binDTO) {
        Bin createdBin = null;
        Bin newBin = new Bin(binDTO.getLongitude(), binDTO.getLatitude(), binDTO.getCapacity(), binDTO.getFillThreshold(), null, null);
        int deviceId = getAvailableDevice();
        if (deviceId == 0) {
            Random random = new Random();
            // There is no IoT Device online so just make up some IoT device
            int randomDeviceId = random.nextInt(2000 - 1001) + 1000;
            newBin.setDeviceId(randomDeviceId);
            createdBin = binRepository.save(newBin);
            loadFakeIoTDeviceData(newBin.getId().intValue(), "getHumidity");
            //loadFakeIoTDeviceData(newBin.getId().intValue(), "getCurrentLevel");
            //loadFakeIoTDeviceData(newBin.getId().intValue(), "getTemperature");

        } else {
            newBin.setDeviceId(deviceId);
            createdBin = binRepository.save(newBin);
        }
        return createdBin;
    }

    /**
     * This function returns an online IoT device that does not belong to any bin
     *
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
            } else return 0;  // Else return 0
        }
    }


    /**
     * This function saves fakes IoT data for a non-existent IoT device
     *
     * @param binId
     * @param payload - what kind of data should be fake loaded
     */
    public void loadFakeIoTDeviceData(int binId, String payload) {
        // Load some fake data
        if (payload.equals("getHumidity")) saveHumidityById(binId, 26.0, LocalDateTime.now());
    }


    /**
     * Evaluates whether a device attached to the bin is active or not
     *
     * @param deviceId
     * @return true/false
     */
    private boolean hasActiveDevice(int deviceId) {
        List<ServerSocketHandler> IoTDevices = tcpServer.getIoTDevices();   // Get all online devices
        if (IoTDevices == null) return false;
        if (IoTDevices.size() == 0) return false;
        for (ServerSocketHandler IoTDevice : IoTDevices) {
            return IoTDevice.getDeviceId() == deviceId;
        }
        return false;
    }

    private void setIotData(int binId, int deviceId, String payload,double fillThreshold) {
        try {
                String responseFromIoT = tcpServer.setDataById(deviceId, payload);
                System.out.println("Response from IoT device: " + deviceId+"which is associated with bin Id: "+binId + " : " + fillThreshold);
        } catch (Exception e) {
            System.out.println("Error communicating with IoT device: " + e.getMessage());
        }
    }

    public void updateBin(UpdateBinDto updatedBinDto) {
        Optional<Bin> binOptional = binRepository.findById(updatedBinDto.getId());
        if (binOptional.isPresent()) {
            Bin bin = binOptional.get();
            try {
                updateBinFields(bin, updatedBinDto);
                bin.setId(updatedBinDto.getId());
                int deviceId = bin.getDeviceId();

                // Check if the device is active
                boolean isActiveDevice = hasActiveDevice(deviceId);

                if (isActiveDevice) {

                        binRepository.save(bin);
                        setIotData(bin.getId().intValue(), deviceId, "setFillThreshold", updatedBinDto.getFillthreshold());
                    }
                    else {
                        throw new IllegalArgumentException("Device ID is active but not available to update the bin.");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }


    public void updateBinFields(Bin bin, UpdateBinDto updatedBinDto) {
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

    public UpdateBinDto convertToBin(Bin bin) {
        UpdateBinDto updatedBinDto = new UpdateBinDto();
        updatedBinDto.setId(bin.getId());
        updatedBinDto.setLongitude(bin.getLongitude());
        updatedBinDto.setLatitude(bin.getLatitude());
        updatedBinDto.setFillthreshold(bin.getFillThreshold());
        return updatedBinDto;
    }


    public boolean isValidLongitude(Double longitude) {
        return longitude >= -180 && longitude <= 180;
    }

    public boolean isValidLatitude(Double latitude) {
        return latitude >= -90 && latitude <= 90;
    }

    public boolean isValidThreshold(Double threshold) {
        return threshold >= 0 && threshold <= 100;
    }

    private double getLastLevelReading(Long binId) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isPresent()) {
            List<Level> alllevel = binOptional.get().getFillLevels();

            if (alllevel != null) {
                alllevel.sort(Comparator.comparing(Level::getDateTime).reversed());
                if (!alllevel.isEmpty()) {
                    return alllevel.get(0).getValue();
                }
            }
            return 0;
        }
        return 0;
    }


}
