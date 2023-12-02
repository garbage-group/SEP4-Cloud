package garbagegroup.cloud.service.serviceImplementation;

import garbagegroup.cloud.DTOs.*;
import garbagegroup.cloud.model.*;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.model.Level;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.service.serviceInterface.IBinService;
import garbagegroup.cloud.tcpserver.ITCPServer;
import garbagegroup.cloud.tcpserver.ServerSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Service
public class BinService implements IBinService {
    ITCPServer tcpServer;
    private IBinRepository binRepository;
    private DTOConverter dtoConverter;


    @Autowired
    public BinService(IBinRepository binRepository, ITCPServer tcpServer) {
        this.binRepository = binRepository;
        this.dtoConverter = new DTOConverter();

        // When creating the BinService, we also start the TCP Server to communicate with the IoT device
        tcpServer.startServer();
        this.setTCPServer(tcpServer);
    }

    public BinService() {}

    /**
     * Fetches SensorData from the IoT device
     * If the reading in DB is newer than 1 hour, it returns it
     * If the reading in DB is older than 1 hour, it gets new data from the IoT device
     * If the IoT device is offline, it fakes the data
     *
     * @param binId
     * @return Optional<Humidity>
     */
    private <T extends SensorData> Optional<T> getCurrentSensorDataByBinId(Long binId, Function<Bin, List<T>> dataExtractor, String payload) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isEmpty()) {
            return Optional.empty();
        }

        Bin bin = binOptional.get();
        List<T> allData = dataExtractor.apply(bin);
        allData.sort(Comparator.comparing(SensorData::getDateTime).reversed());

        LocalDateTime measurementDateTime = (allData.isEmpty()) ? null : allData.get(0).getDateTime();
        if (measurementDateTime == null || isMeasurementOld(measurementDateTime)) {
            getIoTData(bin.getId().intValue(), bin.getDeviceId(), payload);
            bin = binRepository.findById(binId).orElse(bin);
            allData = dataExtractor.apply(bin);
            allData.sort(Comparator.comparing(SensorData::getDateTime).reversed());
        }

        return (allData.isEmpty()) ? Optional.empty() : Optional.of(allData.get(0));
    }

    /**
     * Calls getCurrentSensorDataByBinId (which handles the logic about fetching data)
     *
     * @param binId
     * @return Optional<Humidity>
     */
    @Override
    public Optional<Humidity> getCurrentHumidityByBinId(Long binId) {
        return getCurrentSensorDataByBinId(binId, Bin::getHumidity, "getHumidity");
    }

    /**
     * Calls getCurrentSensorDataByBinId (which handles the logic about fetching data)
     *
     * @param binId
     * @return Optional<Temperature>
     */
    @Override
    public Optional<Temperature> getCurrentTemperatureByBinId(Long binId) {
        return getCurrentSensorDataByBinId(binId, Bin::getTemperatures, "getTemperature");
    }

    /**
     * Calls getCurrentSensorDataByBinId (which handles the logic about fetching data)
     *
     * @param binId
     * @return Optional<Level>
     */
    @Override
    public Optional<Level> getCurrentFillLevelByBinId(Long binId) {
        return getCurrentSensorDataByBinId(binId, Bin::getFillLevels, "getCurrentLevel");
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
     *
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
    public boolean saveHumidityByBinId(int binId, double humidity, LocalDateTime dateTime) {
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

    /**
     * Save fill Level data fetched from the IoT device to the DB
     *
     * @param binId
     * @param fillLevel
     * @param dateTime
     */
    @Override
    public boolean saveFillLevelByBinId(int binId, double fillLevel, LocalDateTime dateTime) {
        System.out.println("About to save fill level: " + fillLevel + " with date and time: " + dateTime + " to bin with ID: " + binId);

        try {
            Optional<Bin> optionalBin = binRepository.findById((long) binId);
            Bin bin = optionalBin.get();
            Level newFillLevel = new Level(bin, fillLevel, dateTime);
            if (bin.getFillLevels() == null) {
                List<Level> fillLevelList = new ArrayList<>();
                fillLevelList.add(newFillLevel);
                bin.setFillLevels(fillLevelList);
            } else bin.getFillLevels().add(newFillLevel);
            try {
                binRepository.save(bin);
                return true;
            } catch (Exception e) {
                System.err.println("Error saving fill level with Bin Id: " + binId + ".\n" + e.getMessage());
                return false;
            }
        } catch(Exception e) {
            System.err.println("Error finding bin with Id: " + binId + ".\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Save fill temperature fetched from the IoT device to the DB
     *
     * @param binId
     * @param temperature
     * @param dateTime
     */
    public boolean saveTemperatureByBinId(int binId, double temperature, LocalDateTime dateTime) {
        System.out.println("About to save fill level: " + temperature + " with date and time: " + dateTime + " to bin with ID: " + binId);

        try {
            Optional<Bin> optionalBin = binRepository.findById((long) binId);
            Bin bin = optionalBin.get();
            Temperature newTemperature = new Temperature(bin, temperature, dateTime);
            if (bin.getTemperatures() == null) {
                List<Temperature> temperatureList = new ArrayList<>();
                temperatureList.add(newTemperature);
                bin.setTemperatures(temperatureList);
            } else bin.getTemperatures().add(newTemperature);
            try {
                binRepository.save(bin);
                return true;
            } catch (Exception e) {
                System.err.println("Error saving temperature with Bin Id: " + binId + ".\n" + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error finding bin with Id: " + binId + ".\n" + e.getMessage());
            return false;
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
            saveHumidityByBinId(binId, humidity, dateTime);
        }
        if (prefix.equals("level")) {
            double humidity = Double.parseDouble(res);
            LocalDateTime dateTime = LocalDateTime.now();
            saveFillLevelByBinId(binId, humidity, dateTime);
        }
        if (prefix.equals("tempe")) {
            double temperature = Double.parseDouble(res);
            LocalDateTime dateTime = LocalDateTime.now();
            saveTemperatureByBinId(binId, temperature, dateTime);
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
        List<BinDto> binDtos = new ArrayList<BinDto>();
        for (Bin bin: bins) {
            BinDto dto = dtoConverter.convertToBinDto(bin);
            binDtos.add(dto);
        }
        return binDtos;
    }

    @Override
    public Optional<BinDto> findBinById(Long id) {
        Optional<Bin> binOptional = binRepository.findById(id);
        if (binOptional.isPresent()) {
            Bin bin = binOptional.get();
            BinDto binDto = dtoConverter.convertToBinDto(bin);
            return Optional.of(binDto);
        } else {
            return Optional.empty();
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
            loadFakeIoTDeviceData(newBin.getId().intValue(), "getTemperature");
            loadFakeIoTDeviceData(newBin.getId().intValue(), "getCurrentLevel");
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
        if (payload.equals("getHumidity")) saveHumidityByBinId(binId, 26.0, LocalDateTime.now());
        if (payload.equals("getTemperature")) saveTemperatureByBinId(binId, 26.0, LocalDateTime.now());
        if (payload.equals("getCurrentLevel")) saveFillLevelByBinId(binId, 37.0, LocalDateTime.now());
    }

    /**
     * Evaluates whether a device attached to the bin is active or not
     *
     * @param deviceId
     * @return true/false
     */
    public boolean hasActiveDevice(int deviceId) {
        List<ServerSocketHandler> IoTDevices = tcpServer.getIoTDevices();   // Get all online devices
        if (IoTDevices == null) return false;
        if (IoTDevices.isEmpty()) return false;
        for (ServerSocketHandler IoTDevice : IoTDevices) {
            return IoTDevice.getDeviceId() == deviceId;
        }
        return false;
    }

    /**
     * Sets the fill threshold data to be sent to the IoT device with the specified payload.
     *
     * @param payload        The payload indicating the type of data being sent to the IoT device
     * @param fillThreshold  The fill threshold value to be communicated to the IoT device
     */
    private void setIotData(String payload,double fillThreshold) {
        try {
            // Communicate the fill threshold data to the IoT device
            String responseFromIoT = tcpServer.setFillThreshold(fillThreshold);
            System.out.println("fill threshold received from IoT device: " + responseFromIoT);
        } catch (Exception e) {
            System.out.println("Error communicating with IoT device: " + e.getMessage());
        }
    }

    /**
     * Updates the Bin information with the provided details in the UpdateBinDto.
     * If the Bin is found and the associated device is active, it saves the updated bin information
     * and sends the fill threshold data to the IoT device.
     *
     * @param updatedBinDto The DTO containing updated information for the Bin
     * @throws IllegalArgumentException When encountering issues during the bin update process or device unavailability
     */
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
                        // Send fill threshold data to the IoT device
                        setIotData("setFillThreshold("+updatedBinDto.getFillthreshold()+")", updatedBinDto.getFillthreshold());
                    }
                    else {
                        binRepository.save(bin);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }


    /**
     * Updates the fields of the Bin object based on the values provided in the UpdateBinDto.
     * Validates and sets the longitude, latitude, and fill threshold values for the Bin.
     *
     * @param bin            The Bin object to be updated
     * @param updatedBinDto  The DTO containing updated values for the Bin
     * @throws IllegalArgumentException if the provided longitude, latitude, or fill threshold is invalid
     *         or if the fill threshold is lower than the last recorded level reading
     */
    public void updateBinFields(Bin bin, UpdateBinDto updatedBinDto) {
        if (isValidLongitude(updatedBinDto.getLongitude())) {
            bin.setLongitude(updatedBinDto.getLongitude());
        } else {
            throw new IllegalArgumentException("longitude should be in between -180 and 180");
        }

        if (isValidLatitude(updatedBinDto.getLatitude())) {
            bin.setLatitude(updatedBinDto.getLatitude());
        } else {
            throw new IllegalArgumentException("latitude should ne between -90 and 90");
        }

        if (isValidThreshold(updatedBinDto.getFillthreshold())) {
            bin.setFillThreshold(updatedBinDto.getFillthreshold());
        } else {
            throw new IllegalArgumentException("threshold value should be between 0 and 100");
        }
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

    /**
     * Retrieves the last recorded level reading for the specified Bin ID.
     * Fetches the most recent level reading value from the database.
     *
     * @param binId The ID of the Bin for which the last level reading is required
     * @return The value of the last recorded level reading or 0 if no readings are available
     */
    private Level getLastLevelReadingWithTimestamp(Long binId) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isPresent()) {
            List<Level> allLevels = binOptional.get().getFillLevels();
            if (allLevels != null) {
                allLevels.sort(Comparator.comparing(Level::getDateTime).reversed());
                if (!allLevels.isEmpty()) {
                    return allLevels.get(0); // Return the level object with the latest timestamp
                }
            }
        }
        return new Level(); // Return an empty Level object if not found
    }


    /**
     * Retrieves a list of bins where the current fill level exceeds the set threshold,
     * generating notification data for each such bin.
     *
     * @return List of NotificationBinDto objects representing bins with fill levels
     *         surpassing their threshold.
     */
    @Override
    public List<NotificationBinDto> getBinsWithThresholdLessThanFillLevel() {
        List<Bin> bins = binRepository.findAll();
        // Initialize an empty list to store notifications
        List<NotificationBinDto> notifications = new ArrayList<>();

        for (Bin bin : bins) {
            boolean isActiveDevice = hasActiveDevice(bin.getDeviceId());

            if (isActiveDevice) {
                // If the device is active, get the current fill level directly from the device
                Level currentLevelFromDevice = getCurrentFillLevelByBinId(bin.getId()).orElse(null);
                if (currentLevelFromDevice != null && currentLevelFromDevice.getValue() > bin.getFillThreshold()) {
                    notifications.add(convertToDTO(bin, currentLevelFromDevice));
                }
            } else {
                // If the device is inactive, retrieve the latest level reading from the database
                Level lastLevelWithTimestamp = getLastLevelReadingWithTimestamp(bin.getId());
                double currentFillLevel = lastLevelWithTimestamp.getValue();
                LocalDateTime timestamp = lastLevelWithTimestamp.getDateTime();

                if (currentFillLevel > bin.getFillThreshold()) {
                    // Create a new Level object using the retrieved values and add it to notifications
                    notifications.add(convertToDTO(bin, new Level(currentFillLevel, timestamp)));
                }
            }
        }
        return notifications;
    }


    private NotificationBinDto convertToDTO(Bin bin, Level latestLevel) {
        return new NotificationBinDto(
                bin.getFillThreshold(),
                bin.getId(),
                latestLevel.getValue(),
                latestLevel.getDateTime()
        );
    }



    }






