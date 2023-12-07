package garbagegroup.cloud.service.serviceImplementation;

import garbagegroup.cloud.DTOs.*;
import garbagegroup.cloud.model.*;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.service.serviceInterface.IBinService;
import garbagegroup.cloud.tcpserver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BinService implements IBinService {
    ITCPServer tcpServer;
    private IBinRepository binRepository;
    private ScheduledExecutorService executorService;

    @Autowired
    public BinService(IBinRepository binRepository, ITCPServer tcpServer) {
        this.binRepository = binRepository;
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
    public String getIoTData(int binId, int deviceId, String payload) {
        String responseFromIoT = "";
        if (hasActiveDevice(deviceId)) {    // Fetch real data from IoT device
            responseFromIoT = tcpServer.getDataById(deviceId, payload);
            handleIoTData(binId, responseFromIoT);
        } else loadFakeIoTDeviceData(binId, payload);     // Or load some fake data
        return responseFromIoT;
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
            } else bin.getHumidity().add(newHumidity);

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
        } catch (Exception e) {
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
    public boolean deleteBinById(long binId) {
        if (binRepository.existsById(binId)) {
            binRepository.deleteById(binId);
            return true;
        } else {
            throw new NoSuchElementException("Bin with id " + binId + " not found");
        }
    }

    public BinDto convertToBinDtoAndSetValues(Bin bin) {
        BinDto dto = DTOConverter.convertToBinDto(bin);
        // Set status
        boolean deviceStatus = false;
        try {
            deviceStatus = getDeviceStatusByBinId(bin.getId());
        } catch (NoSuchElementException e) {
            dto.setStatus("OFFLINE");
        }
        if (!dto.getStatus().equals("OFFLINE")) dto.setStatus(deviceStatus ? "ACTIVE" : "ERROR");

        // Set pickup date
        dto.setPickUpTime(setPickupDate(bin));

        // Set last emptied time
        dto.setEmptiedLast(setLastEmptiedTime(bin));

        return dto;
    }

    @Override
    public List<BinDto> findAllBins() {
        List<Bin> bins = binRepository.findAll();
        List<BinDto> binDtos = new ArrayList<>();
        for (Bin bin : bins) {
            BinDto dto = convertToBinDtoAndSetValues(bin);

            binDtos.add(dto);
        }
        return binDtos;
    }

    public LocalDateTime setPickupDate(Bin bin) {
        //check the fill level of bin from the database and if it exceeds the threshold, set the pickup date to tomorrow
        Level lastLevelWithTimestamp = getLastLevelReadingWithTimestamp(bin.getId());
        double currentFillLevel = lastLevelWithTimestamp.getValue();
        if (currentFillLevel > bin.getFillThreshold()) {
            LocalDateTime timestamp = lastLevelWithTimestamp.getDateTime();
            //if the fill level time is after 14:00, set the pickup date to tomorrow otherwise set it after two hours
            if (timestamp.getHour() >= 14) {
                bin.setPickUpTime(timestamp.plusDays(1));
            } else {
                bin.setPickUpTime(timestamp.plusHours(3));
            }
            binRepository.save(bin);
        }
        return bin.getPickUpTime();
    }

    public LocalDateTime setLastEmptiedTime(Bin bin) {
        //check the last pickup date of bin and set the last emptied date to the same date
        LocalDateTime lastPickupTime = binRepository.findLastPickupTime(bin.getId());
        if (lastPickupTime != null) {
            bin.setEmptiedLast(lastPickupTime);
            binRepository.save(bin);
        }
        return bin.getEmptiedLast();
    }

    @Override
    public Optional<BinDto> findBinById(Long id) {
        Optional<Bin> binOptional = binRepository.findById(id);
        if (binOptional.isPresent()) {
            Bin bin = binOptional.get();
            BinDto dto = convertToBinDtoAndSetValues(bin);
            return Optional.of(dto);
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
        Bin createdBin;
        Bin newBin = new Bin(binDTO.getLongitude(), binDTO.getLatitude(), binDTO.getCapacity(), binDTO.getFillThreshold(), null, null);
        int deviceId = getAvailableDevice();
        if (deviceId == 0) {
            Random random = new Random();
            // There is no IoT Device online so just make up some IoT device
            int randomDeviceId = random.nextInt(2000 - 1001) + 1000;
            newBin.setDeviceId(randomDeviceId);
            createdBin = binRepository.save(newBin);
            createdBin.setDeviceId(randomDeviceId);
            loadFakeIoTDeviceData(createdBin.getId().intValue(), "getHumidity");
            loadFakeIoTDeviceData(createdBin.getId().intValue(), "getTemperature");
            loadFakeIoTDeviceData(createdBin.getId().intValue(), "getCurrentLevel");
        } else {
            newBin.setDeviceId(deviceId);
            createdBin = binRepository.save(newBin);
            createdBin.setDeviceId(deviceId);
            if (!tcpServer.setIoTData(deviceId, "calibrateDevice")) {
                // Try to calibrate the device and if it returns false, something went wrong - but we don't even throw exception because it is not so important
                System.out.println("The device could not be calibrated");
            }
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
        if (IoTDevices.isEmpty()) return 0;
        else {
            List<Bin> bins = binRepository.findAll();   // Fetch all bins
            for (Bin bin : bins) {      // Check if there is an online device that has not been assigned to a bin
                // Remove a device from list IoTDevices if the device belongs to a bin already
                IoTDevices.removeIf(device -> bin.getDeviceId() == device.getDeviceId());
            }
            if (!IoTDevices.isEmpty()) { // If there is a device left, return the device ID of the first device
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
            if (IoTDevice.getDeviceId() == deviceId) return true;
        }
        return false;
    }

    /**
     * Updates the Bin information with the provided details in the UpdateBinDto.
     * If the Bin is found and the associated device is active, it saves the updated bin information
     * and sends the fill threshold data to the IoT device.
     *
     * @param updatedBinDto The DTO containing updated information for the Bin
     * @return
     * @throws IllegalArgumentException When encountering issues during the bin update process or device unavailability
     */
    public boolean updateBin(UpdateBinDto updatedBinDto) {
        Optional<Bin> binOptional = binRepository.findById(updatedBinDto.getId());
        if (binOptional.isPresent()) {
            Bin bin = binOptional.get();
            try {
                updateBinFields(bin, updatedBinDto);
                bin.setId(updatedBinDto.getId());
                binRepository.save(bin);
                return true;
            } catch (Exception e) {
                System.err.println("Error while updating bin with id " + bin.getId() + e.getMessage());
                return false;
            }
        }
        throw new NoSuchElementException();
        //TODO: catch this exception
    }

    /**
     * Updates the fields of the Bin object based on the values provided in the UpdateBinDto.
     * Validates and sets the longitude, latitude, and fill threshold values for the Bin.
     *
     * @param bin           The Bin object to be updated
     * @param updatedBinDto The DTO containing updated values for the Bin
     * @throws IllegalArgumentException if the provided longitude, latitude, or fill threshold is invalid
     *                                  or if the fill threshold is lower than the last recorded level reading
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
    public Level getLastLevelReadingWithTimestamp(Long binId) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isPresent()) {
            List<Level> allLevels = binOptional.get().getFillLevels();
            if (!allLevels.isEmpty()) {
                allLevels.sort(Comparator.comparing(Level::getDateTime).reversed());
                return allLevels.get(0); // Return the level object with the latest timestamp
            }
        }
        return new Level(); // Return an empty Level object if not found
    }


    /**
     * Retrieves a list of bins where the current fill level exceeds the set threshold
     *
     * @return List of NotificationBinDto objects representing bins with fill levels
     * surpassing their threshold.
     */
    @Override
    public List<NotificationBinDto> getBinsWithThresholdLessThanFillLevel() {
        List<Bin> bins = binRepository.findAll();

        return bins.stream()
                .map(this::verifyBinsFillLevel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Helped method to getBinsWithThresholdLessThanFillLevel
     * If the bin has an active device, it requests fill level data from it and if the data is over the threshold of the bin, it returns the Level
     * If the bin has inactive device, it fetches the last data from DB, and if it is above the threshold, it returns the Level
     * Else returns null, so it is not saved as an alarming value in the stream in getBinsWithThresholdLessThanFillLevel
     **
     * @param bin
     * @return NotificationBinDto
     */
    public NotificationBinDto verifyBinsFillLevel(Bin bin) {
        boolean isActiveDevice = hasActiveDevice(bin.getDeviceId());

        if (isActiveDevice) {
            Level currentLevelFromDevice = getCurrentFillLevelByBinId(bin.getId()).orElse(null);
            if (currentLevelFromDevice != null && currentLevelFromDevice.getValue() > bin.getFillThreshold()) {
                return convertToDTO(bin, currentLevelFromDevice);
            }
        } else {
            Level lastLevelWithTimestamp = getLastLevelReadingWithTimestamp(bin.getId());
            double currentFillLevel = lastLevelWithTimestamp.getValue();

            if (currentFillLevel > bin.getFillThreshold()) {
                LocalDateTime timestamp = lastLevelWithTimestamp.getDateTime();
                return convertToDTO(bin, new Level(currentFillLevel, timestamp));
            }
        }
        return null;
    }

    /**
     * Gets Status directly from the IoT device
     * There is no communication with the DB here, because the status has to come directly from the IoT
     * If there is no connection to the IoT, it just returns it as offline
     * @param binId
     * @return true (active)/ false (not-active)
     */
    @Override
    public boolean getDeviceStatusByBinId(Long binId) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isPresent()) {
            Bin bin = binOptional.get();
            // Send fill threshold data to the IoT device
            String response = getIoTData(binId.intValue(), bin.getDeviceId(), "getStatus");
            if (response.equals("statu:OK")) return true;
            else if (response.equals("statu:NOT OK")) return false;
            else throw new NoSuchElementException("The device on bin " + binId + " is offline");
        } else throw new NoSuchElementException("Bin with id " + binId + " not found");
    }

    public NotificationBinDto convertToDTO(Bin bin, Level latestLevel) {
        return new NotificationBinDto(
                bin.getFillThreshold(),
                bin.getId(),
                latestLevel.getValue(),
                latestLevel.getDateTime()
        );
    }

    /**
     * Starts a service that requests current level of connected devices
     * @param intervalSeconds interval in which the data is requested in seconds
     */
    public void startPeriodicLevelRequest(int intervalSeconds) {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(this::requestCurrentLevels, 0, intervalSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops a service that requests current level of connected devices
     */
    public void stopPeriodicLevelRequest() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * Iterates through a list of connected devices and requests the current fill levels
     * based on a matching binID and saves them to database.
     */
    public void requestCurrentLevels() {
        try {
            List<BinDto> bins = findAllBins();
            List<ServerSocketHandler> devices = tcpServer.getIoTDevices();

            for (ServerSocketHandler device : devices) {
                System.out.println("Requesting current level from device " + device.getDeviceId());

                // Find the bin that matches the device ID
                BinDto matchingBin = bins.stream()
                        .filter(bin -> bin.getDeviceId() == device.getDeviceId())
                        .findFirst()
                        .orElse(null);

                if (matchingBin != null) {
                    String response = device.sendMessage("getCurrentLevel");
                    handleIoTData(matchingBin.getId().intValue(), response);
                } else {
                    System.out.println("No matching bin found for device ID " + device.getDeviceId());
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error while trying periodical level retrieval of connected devices.");
        }
    }
}






