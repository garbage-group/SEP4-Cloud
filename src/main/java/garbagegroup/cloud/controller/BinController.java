package garbagegroup.cloud.controller;

import garbagegroup.cloud.DTOs.BinDto;
import garbagegroup.cloud.DTOs.CreateBinDTO;
import garbagegroup.cloud.DTOs.NotificationBinDto;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.DTOs.UpdateBinDto;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.model.Level;
import garbagegroup.cloud.model.Temperature;
import garbagegroup.cloud.service.serviceInterface.IBinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/bins")
@CrossOrigin // Needed to send requests from/to URLs on different ports (so for the frontend, because IoT doesn't care since it is TCP)
public class BinController {
    private IBinService binService;
    private Logger logger = LoggerFactory.getLogger(BinController.class);

    @Autowired
    public BinController(IBinService binService) {
        this.binService = binService;
    }


    /**
     * The method tries to retrieve humidity information from a service based on a provided ID,
     * handles cases where the humidity is present or not,
     * and logs and returns an error response in case of an exception.
     */

    @GetMapping("/{id}/humidity")
    public ResponseEntity<Humidity> getCurrentHumidityByBinId(@PathVariable Long id) {
        try {
            Optional<Humidity> humidity = binService.getCurrentHumidityByBinId(id);

            return humidity.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } catch (Exception e) {
            logger.error("Error retrieving humidity for bin with id {}", id, e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}/temperature")
    public ResponseEntity<Temperature> getCurrentTemperatureByBinId(@PathVariable Long id) {
        try {
            Optional<Temperature> temperature = binService.getCurrentTemperatureByBinId(id);

            return temperature.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } catch (Exception e) {
            logger.error("Error retrieving humidity for bin with id {}", id, e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}/fill_level")
    public ResponseEntity<Level> getCurrentFillLevelByBinId(@PathVariable Long id) {
        try {
            Optional<Level> fillLevel = binService.getCurrentFillLevelByBinId(id);

            return fillLevel.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } catch (Exception e) {
            logger.error("Error retrieving humidity for bin with id {}", id, e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Handles POST request for creating a bin
     * @param binDTO
     * @return The created bin
     */
    @PostMapping
    public ResponseEntity<Bin> createBin(@RequestBody CreateBinDTO binDTO) {
        try {
            Bin createdBin = binService.create(binDTO);
            return new ResponseEntity<>(createdBin, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<BinDto>> getAllBins() {
        try {
            List<BinDto> bins = binService.findAllBins();
            return new ResponseEntity<>(bins, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching all bins", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BinDto> getBinById(@PathVariable Long id) {
        try {
            Optional<BinDto> bin = binService.findBinById(id);
            return bin.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error occurred while fetching bin with ID: " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteBinById(@PathVariable Long id) {
        try {
            binService.deleteBinById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            logger.error("Error while deleting: Bin with ID " + id + " not found.", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error occurred while deleting bin with ID: " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{binId}")
    public ResponseEntity<String> updateBin(@PathVariable Long binId, @RequestBody UpdateBinDto updatedBinDto) {
        try {
            updatedBinDto.setId(binId);
            binService.updateBin(updatedBinDto);
            return ResponseEntity.ok("Bin updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating bin: " + e.getMessage());
        }
    }



    @GetMapping("/notification")
    public ResponseEntity<List<NotificationBinDto>> getBinsWithThresholdLessThanFillLevel() {
        try {
            List<NotificationBinDto> bins = binService.getBinsWithThresholdLessThanFillLevel();
            return new ResponseEntity<>(bins, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching all bins", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

