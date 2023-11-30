package garbagegroup.cloud.service.serviceImplementation;

import garbagegroup.cloud.DTOs.DTOConverter;
import garbagegroup.cloud.DTOs.BinDto;
import garbagegroup.cloud.DTOs.CreateBinDTO;
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
    public boolean saveHumidityById(int binId, double humidity, LocalDateTime dateTime) {
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
    public boolean hasActiveDevice(int deviceId) {
        List<ServerSocketHandler> IoTDevices = tcpServer.getIoTDevices();   // Get all online devices
        if (IoTDevices == null) return false;
        if (IoTDevices.isEmpty()) return false;
        for (ServerSocketHandler IoTDevice : IoTDevices) {
            return IoTDevice.getDeviceId() == deviceId;
        }
        return false;
    }
}
