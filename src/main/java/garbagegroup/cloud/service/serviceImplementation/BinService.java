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

    public BinService() {
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


    /**
     * Updates the Bin entity based on the provided information in the {@code updatedBinDto}.
     * Retrieves the Bin by its ID from the repository, updates its fields, and saves the changes.
     * Additionally, communicates with the IoT device associated with the Bin to update its fill threshold.
     *
     * @param updatedBinDto An instance of {@link UpdateBinDto} containing the updated information.
     *                      Requires a valid ID matching an existing Bin in the repository.
     *                      Should contain longitude, latitude, and fill threshold updates.
     * @throws IllegalArgumentException If the updated bin fields (longitude, latitude, fill threshold) are invalid.
     *                                  This occurs when the provided values are outside the valid range.
     * @throws IllegalStateException    If the Bin is not associated with any IoT device, throwing an IllegalStateException.
     *                                  This indicates that the Bin lacks an association with an IoT device.
     * @throws Exception                If an error occurs while updating the Bin or communicating with the IoT device.
     *                                  This captures any unexpected exceptions during the process.
     */
    public void updateBin(UpdateBinDto updatedBinDto) {
        Optional<Bin> binOptional = binRepository.findById(updatedBinDto.getId());
        binOptional.ifPresent(bin -> {
            try {
                updateBinFields(bin, updatedBinDto);
                binRepository.save(bin);
                communicateWithIoTToUpdateBin(bin.getId(), bin.getFillThreshold());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Error updating bin: " + e.getMessage());
            }
        });
    }

    /**
     * Updates the fields of the provided Bin entity based on the information in the {@code updatedBinDto}.
     * Validates and sets the longitude, latitude, and fill threshold values in the Bin entity.
     * Additionally, ensures that the fill threshold is within the valid range and not set lower than the last level reading.
     *
     * @param bin            The {@link Bin} entity to be updated.
     * @param updatedBinDto  An instance of {@link UpdateBinDto} containing updated information.
     *                       Contains longitude, latitude, and fill threshold updates.
     * @throws IllegalArgumentException If any of the updated bin fields (longitude, latitude, fill threshold) are invalid.
     *                                  This occurs when the provided values are outside the valid range.
     * @throws IllegalArgumentException If the fill threshold value is set lower than the last level reading.
     *                                  This ensures that the new fill threshold is higher than or equal to the last level reading.
     * @see Bin#setLongitude(Double)
     * @see Bin#setLatitude(Double)
     * @see Bin#setFillThreshold(Double)
     */

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

    /**
     * Communicates with the IoT device to update the fill threshold of the associated bin.
     * Sends the fill threshold value to the IoT device and handles the response received.
     *
     * @param binId         The ID of the bin to update on the IoT device.
     * @param fillThreshold The new fill threshold value to be set for the bin.
     * @see ITCPServer#setFillThreshold(Long, double)
     */
    private void communicateWithIoTToUpdateBin(Long binId,double fillThreshold) {
        try {
            String response = tcpServer.setFillThreshold(binId,fillThreshold);
            System.out.println("Response from IoT device: " + response+" : "+fillThreshold);
        } catch (Exception e) {
            System.out.println("Error communicating with IoT device: " + e.getMessage());
        }
    }


    /**
     * Converts an UpdateBinDto object to a Bin object.
     *
     * @param updatedBinDto The UpdateBinDto object containing updated bin information.
     * @return A Bin object with values populated from the UpdateBinDto.
     */
    private Bin convertToBin(UpdateBinDto updatedBinDto) {
        Bin bin = new Bin();
        bin.setId(updatedBinDto.getId());
        bin.setLongitude(updatedBinDto.getLongitude());
        bin.setLatitude(updatedBinDto.getLatitude());
        bin.setFillThreshold(updatedBinDto.getFillthreshold());
        return bin;
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
     * Retrieves the last level reading value for a given bin ID.
     *
     * @param binId The ID of the bin to fetch the last level reading from.
     * @return The value of the last level reading for the specified bin ID. Returns 0 if no level readings are available.
     */
    private double getLastLevelReading(Long binId) {
        Optional<Bin> binOptional = binRepository.findById(binId);
        if (binOptional.isPresent()) {
            List<Level> alllevel = binOptional.get().getLevel();

            if (alllevel != null) { // Check if alllevel is not null
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
